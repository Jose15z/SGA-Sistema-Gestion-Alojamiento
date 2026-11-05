#!/usr/bin/env python3
"""Verificación HTTP local de la guía 12. No imprime tokens ni la clave JWT.

Uso: python3 scripts/verificar_guia12.py [--base-url http://localhost:8080]
Guarda evidencias sanitizadas en docs/evidencias/guia12/http-resultados.json.
Los tokens auxiliares se guardan únicamente en build/guia12 (ignorado por Git).
"""
import argparse
import base64
import hashlib
import hmac
import json
import os
from pathlib import Path
import time
from datetime import datetime, timezone
from urllib.request import Request, urlopen
from urllib.error import HTTPError

ROOT = Path(__file__).resolve().parents[1]


def b64(data):
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def main():
    args = argparse.ArgumentParser(description=__doc__)
    args.add_argument("--base-url", default="http://localhost:8080")
    options = args.parse_args()
    # Esta herramienta firma tokens deliberadamente inválidos solo para la API local.
    if not options.base_url.startswith(("http://localhost:", "http://127.0.0.1:")):
        raise SystemExit("Esta verificación solo admite una API local de desarrollo")
    results = []

    def request(name, method, path, expected, body=None, token=None):
        headers = {"Content-Type": "application/json"}
        if token:
            headers["Authorization"] = "Bearer " + token
        req = Request(options.base_url + path, headers=headers, method=method,
                      data=json.dumps(body).encode() if body is not None else None)
        try:
            response = urlopen(req, timeout=15)
        except HTTPError as error:
            response = error
        raw = response.read().decode()
        payload = json.loads(raw) if raw else None
        # Nunca guardar el token devuelto por el login, ni encabezados Authorization.
        safe = dict(payload) if isinstance(payload, dict) else payload
        if isinstance(safe, dict) and "token" in safe:
            safe["token"] = "[OMITIDO]"
        results.append(dict(caso=name, metodo=method, ruta=path, esperado=expected,
                            obtenido=response.status, correcto=response.status == expected,
                            respuesta=safe))
        return payload

    def login(name, email, password):
        data = request("Login " + name, "POST", "/api/auth/login", 200,
                       {"correo": email, "contrasena": password})
        if not data or "token" not in data:
            raise SystemExit("No fue posible autenticar a " + name)
        return data["token"]

    reception = login("recepción", "recepcion@sieteraices.co", "Recepcion123*")
    guest1 = login("huésped 1", "ana.ramirez@correo.com", "Huesped123*")
    guest2 = login("huésped 2", "carlos.gomez@correo.com", "Huesped123*")
    service = login("personal de servicio", "servicio@sieteraices.co", "Servicio123*")
    request("Contraseña incorrecta", "POST", "/api/auth/login", 401,
            {"correo": "recepcion@sieteraices.co", "contrasena": "Incorrecta123"})
    request("Correo inválido", "POST", "/api/auth/login", 400,
            {"correo": "sin-formato", "contrasena": "Incorrecta123"})
    request("Sin token", "GET", "/api/reservas?estado=PENDIENTE", 401)
    request("Listado prohibido a huésped", "GET", "/api/reservas?estado=PENDIENTE", 403, token=guest1)
    request("Listado de recepción", "GET", "/api/reservas?estado=PENDIENTE", 200, token=reception)
    request("Reserva propia", "GET", "/api/reservas/RES-2025-00001", 200, token=guest1)
    request("Reserva ajena", "GET", "/api/reservas/RES-2025-00001", 403, token=guest2)
    request("Recurso inexistente para recepción", "GET", "/api/reservas/RES-2026-99999", 404, token=reception)
    request("Recurso inexistente para huésped", "GET", "/api/reservas/RES-2026-99999", 403, token=guest1)
    request("Personal de servicio no confirma", "PUT", "/api/reservas/RES-2025-00001/confirmar", 403, token=service)
    request("Folio ajeno", "GET", "/api/folios/RES-2025-00001", 403, token=guest2)
    altered = reception.rsplit(".", 1)[0] + ".firma-alterada"
    request("Token alterado", "GET", "/api/reservas", 401, token=altered)

    secret = os.environ.get("SGA_JWT_SECRET")
    if secret is None:
        local = ROOT / "src/main/resources/application-secrets.properties"
        if local.exists():
            for line in local.read_text().splitlines():
                if line.startswith("sga.jwt.secret="):
                    secret = line.split("=", 1)[1].strip()
    if not secret:
        raise SystemExit("Se necesita la misma clave local de la aplicación para probar expiración y emisor")
    # Copiar claims del login; solo cambiar fecha o emisor, conservando firma válida.
    claims = json.loads(base64.urlsafe_b64decode(reception.split(".")[1] + "=="))

    def signed(payload):
        content = b64(b'{"alg":"HS256"}') + "." + b64(json.dumps(payload).encode())
        return content + "." + b64(hmac.new(secret.encode(), content.encode(), hashlib.sha256).digest())

    expired = signed(dict(claims, iat=int(time.time()) - 7200, exp=int(time.time()) - 120))
    foreign = signed(dict(claims, iss="emisor-ajeno"))
    request("Token vencido con firma válida", "GET", "/api/reservas", 401, token=expired)
    request("Emisor incorrecto con firma válida", "GET", "/api/reservas", 401, token=foreign)
    request("Algoritmo none", "GET", "/api/reservas", 401,
            token="eyJhbGciOiJub25lIn0.eyJzdWIiOiJhZG1pbiJ9.")
    # Material auxiliar para el caso 14 del archivo .http; build/ está ignorado.
    private = ROOT / "build/guia12/tokens.json"
    private.parent.mkdir(parents=True, exist_ok=True)
    private.write_text(json.dumps({"vencido": expired, "alterado": altered}, indent=2) + "\n")
    private.chmod(0o600)
    report = {"fechaUtc": datetime.now(timezone.utc).isoformat(), "baseUrl": options.base_url,
              "casos": len(results), "aprobados": sum(r["correcto"] for r in results), "resultados": results}
    output = ROOT / "docs/evidencias/guia12/http-resultados.json"
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n")
    for item in results:
        print(f"{'OK' if item['correcto'] else 'FALLO':5} {item['obtenido']} {item['caso']}")
    print(f"{report['aprobados']}/{report['casos']} casos aprobados")
    raise SystemExit(0 if report["aprobados"] == report["casos"] else 1)


if __name__ == "__main__":
    main()
