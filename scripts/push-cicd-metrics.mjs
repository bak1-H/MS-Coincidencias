// Empuja metricas de CI/CD al Pushgateway para visualizarlas en Grafana (IE3).
//
// Que empuja:
//   - cicd_test_coverage_percent : cobertura de lineas leida del reporte JaCoCo
//   - cicd_deploy_duration_seconds : tiempo del ultimo despliegue (docker compose up)
//
// Uso:
//   node scripts/push-cicd-metrics.mjs --deploy-seconds 87
//   node scripts/push-cicd-metrics.mjs --jacoco target/site/jacoco/jacoco.xml --deploy-seconds 87
//
// Variables de entorno:
//   PUSHGATEWAY_URL  (por defecto http://localhost:9091)

import { readFileSync } from "node:fs";

const args = process.argv.slice(2);
function arg(name, fallback) {
  const i = args.indexOf(`--${name}`);
  return i !== -1 && args[i + 1] ? args[i + 1] : fallback;
}

const PUSHGATEWAY_URL = process.env.PUSHGATEWAY_URL || "http://localhost:9091";
const jacocoPath = arg("jacoco", "target/site/jacoco/jacoco.xml");
const deploySeconds = Number(arg("deploy-seconds", "0"));

// Lee la cobertura de lineas del reporte JaCoCo (counter type="LINE" a nivel <report>)
function readLineCoverage(path) {
  let xml;
  try {
    xml = readFileSync(path, "utf8");
  } catch {
    console.warn(`[aviso] No se encontro ${path}. Corre primero: ./mvnw test`);
    return null;
  }
  // El ultimo counter LINE del archivo corresponde al total del reporte
  const matches = [...xml.matchAll(/<counter type="LINE" missed="(\d+)" covered="(\d+)"\/>/g)];
  if (matches.length === 0) return null;
  const last = matches[matches.length - 1];
  const missed = Number(last[1]);
  const covered = Number(last[2]);
  const total = missed + covered;
  if (total === 0) return 0;
  return (covered / total) * 100;
}

async function push(metricsBody) {
  const url = `${PUSHGATEWAY_URL}/metrics/job/ci-cd/instance/ms-coincidencias`;
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "text/plain" },
    body: metricsBody,
  });
  if (!res.ok) {
    throw new Error(`Pushgateway respondio ${res.status}: ${await res.text()}`);
  }
}

const lines = [];

const coverage = readLineCoverage(jacocoPath);
if (coverage !== null) {
  lines.push("# TYPE cicd_test_coverage_percent gauge");
  lines.push(`cicd_test_coverage_percent ${coverage.toFixed(2)}`);
  console.log(`Cobertura de pruebas: ${coverage.toFixed(2)}%`);
}

if (deploySeconds > 0) {
  lines.push("# TYPE cicd_deploy_duration_seconds gauge");
  lines.push(`cicd_deploy_duration_seconds ${deploySeconds}`);
  console.log(`Tiempo de despliegue: ${deploySeconds}s`);
}

if (lines.length === 0) {
  console.error("Nada que empujar. Genera el reporte JaCoCo y/o pasa --deploy-seconds.");
  process.exit(1);
}

try {
  await push(lines.join("\n") + "\n");
  console.log(`Metricas enviadas a ${PUSHGATEWAY_URL}`);
} catch (err) {
  console.error(`Error enviando metricas: ${err.message}`);
  process.exit(1);
}
