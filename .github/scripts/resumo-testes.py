#!/usr/bin/env python3
"""Escreve no $GITHUB_STEP_SUMMARY o resultado dos testes e a cobertura do JaCoCo."""
import csv
import glob
import os
import sys
import xml.etree.ElementTree as ET


def ler_suites(pasta):
    suites = []
    for arquivo in sorted(glob.glob(os.path.join(pasta, "TEST-*.xml"))):
        raiz = ET.parse(arquivo).getroot()
        total = int(raiz.get("tests", 0))
        falhas = int(raiz.get("failures", 0)) + int(raiz.get("errors", 0))
        puladas = int(raiz.get("skipped", 0))
        nome = raiz.get("name", "").rsplit(".", 1)[-1]
        suites.append((nome, total, total - falhas - puladas, falhas, puladas, float(raiz.get("time", 0))))
    return suites


def cobertura(csv_path):
    if not csv_path or not os.path.exists(csv_path):
        return None
    perdidas = cobertas = 0
    with open(csv_path, newline="", encoding="utf-8") as f:
        for linha in csv.DictReader(f):
            perdidas += int(linha["INSTRUCTION_MISSED"])
            cobertas += int(linha["INSTRUCTION_COVERED"])
    return 100.0 * cobertas / max(perdidas + cobertas, 1)


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    titulo, pasta = sys.argv[1], sys.argv[2]
    csv_path = sys.argv[3] if len(sys.argv) > 3 else None
    suites = ler_suites(pasta)

    total = sum(s[1] for s in suites)
    passaram = sum(s[2] for s in suites)
    falharam = sum(s[3] for s in suites)
    icone = "✅" if suites and falharam == 0 else "❌"

    linhas = [f"## {icone} {titulo}", ""]
    if not suites:
        linhas.append(f"Nenhum relatório encontrado em `{pasta}`.")
    else:
        linhas += [
            f"**{passaram}/{total} testes passaram**, {falharam} falha(s).",
            "",
            "| Classe | Testes | ✅ | ❌ | ⏭️ | Tempo (s) |",
            "|---|---:|---:|---:|---:|---:|",
        ]
        linhas += [f"| `{n}` | {t} | {p} | {f} | {s} | {tempo:.2f} |" for n, t, p, f, s, tempo in suites]
    pct = cobertura(csv_path)
    if pct is not None:
        linhas += ["", f"**Cobertura de instruções (JaCoCo):** {pct:.1f}%"]

    destino = os.environ.get("GITHUB_STEP_SUMMARY")
    texto = "\n".join(linhas) + "\n"
    if destino:
        with open(destino, "a", encoding="utf-8") as f:
            f.write(texto)
    print(texto)


if __name__ == "__main__":
    main()
