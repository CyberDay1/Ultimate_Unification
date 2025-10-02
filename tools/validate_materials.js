#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const schemaPath = path.join(__dirname, '..', 'DOCS', 'materials.schema.json');
const targetPath = process.argv[2] || path.join('src', 'main', 'resources', 'data', 'unifyworks', 'unify', 'materials.json');

function readJson(file) {
  try {
    return JSON.parse(fs.readFileSync(file, 'utf8'));
  } catch (err) {
    console.error(`Failed to read ${file}:`, err.message);
    process.exit(1);
  }
}

function validate(schema, data, pointer = '$') {
  const errors = [];

  if (schema.const !== undefined && JSON.stringify(data) !== JSON.stringify(schema.const)) {
    errors.push(`${pointer}: expected constant ${JSON.stringify(schema.const)}`);
    return errors;
  }

  if (schema.enum && !schema.enum.some((candidate) => JSON.stringify(candidate) === JSON.stringify(data))) {
    errors.push(`${pointer}: value ${JSON.stringify(data)} not in enum`);
    return errors;
  }

  const type = schema.type;
  if (type) {
    if (type === 'object') {
      if (typeof data !== 'object' || data === null || Array.isArray(data)) {
        errors.push(`${pointer}: expected object`);
        return errors;
      }

      if (Array.isArray(schema.required)) {
        for (const key of schema.required) {
          if (!(key in data)) {
            errors.push(`${pointer}: missing required property '${key}'`);
          }
        }
      }

      const props = schema.properties || {};
      for (const [key, value] of Object.entries(data)) {
        if (props[key]) {
          errors.push(...validate(props[key], value, `${pointer}.${key}`));
        } else if (schema.additionalProperties === false) {
          errors.push(`${pointer}: unexpected property '${key}'`);
        } else if (typeof schema.additionalProperties === 'object') {
          errors.push(...validate(schema.additionalProperties, value, `${pointer}.${key}`));
        }
      }
      return errors;
    }

    if (type === 'array') {
      if (!Array.isArray(data)) {
        errors.push(`${pointer}: expected array`);
        return errors;
      }
      if (schema.items) {
        data.forEach((item, idx) => {
          errors.push(...validate(schema.items, item, `${pointer}[${idx}]`));
        });
      }
      if (schema.uniqueItems) {
        const seen = new Set();
        data.forEach((item, idx) => {
          const key = JSON.stringify(item);
          if (seen.has(key)) {
            errors.push(`${pointer}[${idx}]: duplicate item ${JSON.stringify(item)}`);
          }
          seen.add(key);
        });
      }
      return errors;
    }

    if (type === 'string') {
      if (typeof data !== 'string') {
        errors.push(`${pointer}: expected string`);
        return errors;
      }
      if (schema.pattern) {
        const regex = new RegExp(schema.pattern);
        if (!regex.test(data)) {
          errors.push(`${pointer}: string '${data}' does not match pattern ${schema.pattern}`);
        }
      }
      return errors;
    }

    if (type === 'boolean') {
      if (typeof data !== 'boolean') {
        errors.push(`${pointer}: expected boolean`);
      }
      return errors;
    }

    if (type === 'number' || type === 'integer') {
      if (typeof data !== 'number' || (type === 'integer' && !Number.isInteger(data))) {
        errors.push(`${pointer}: expected ${type}`);
      }
      return errors;
    }
  }

  return errors;
}

const schema = readJson(schemaPath);
const data = readJson(targetPath);
const errors = validate(schema, data);

if (errors.length > 0) {
  console.error('materials.json validation failed:');
  for (const err of errors) {
    console.error(` - ${err}`);
  }
  process.exit(1);
}

console.log(`${path.relative(process.cwd(), targetPath)} is valid against materials.schema.json`);
