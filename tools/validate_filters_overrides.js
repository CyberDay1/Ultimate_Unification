#!/usr/bin/env node
const fs = require('fs');
const path = require('path');
const Ajv = require('ajv');

const ajv = new Ajv({allErrors: true, strict: false});

const readJSON = (p) => JSON.parse(fs.readFileSync(p, 'utf8'));
const root = process.cwd();
const filtersSchema = readJSON(path.join(root, 'DOCS', 'filters.schema.json'));
const overridesSchema = readJSON(path.join(root, 'DOCS', 'overrides.schema.json'));
const validateFilters = ajv.compile(filtersSchema);
const validateOverrides = ajv.compile(overridesSchema);

const scanJSON = (dir) => {
  if (!fs.existsSync(dir)) return [];
  const out = [];
  for (const entry of fs.readdirSync(dir, {withFileTypes: true})) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      out.push(...scanJSON(full));
    } else if (entry.isFile() && entry.name.endsWith('.json')) {
      out.push(full);
    }
  }
  return out;
};

const dataRoot = path.join('src', 'main', 'resources', 'data');
const filters = scanJSON(dataRoot).filter(p => p.endsWith(path.join('unify', 'filters.json')));
const overrides = scanJSON(dataRoot).filter(p => p.includes(path.join('unify', 'overrides')));

let ok = true;
for (const file of filters) {
  try {
    const payload = readJSON(file);
    if (!validateFilters(payload)) {
      console.error('[filters]', file, validateFilters.errors);
      ok = false;
    }
  } catch (err) {
    console.error('[filters]', file, err.message);
    ok = false;
  }
}

for (const file of overrides) {
  try {
    const payload = readJSON(file);
    if (!validateOverrides(payload)) {
      console.error('[overrides]', file, validateOverrides.errors);
      ok = false;
    }
  } catch (err) {
    console.error('[overrides]', file, err.message);
    ok = false;
  }
}

if (!ok) {
  process.exit(1);
}

console.log('filters/overrides look valid.');
