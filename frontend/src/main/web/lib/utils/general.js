/**
 * Turn an object into two arrays. One is keys and one is the matching values.
 * @param obj
 * @returns {{keys: Array, values: Array}}
 */
export function objectToKeysAndValuesArray(obj) {
  if (!obj) {
    return {
      keys: [],
      values: []
    }
  }

  const keys = Object.keys(obj)
  const values = keys.map(key => obj[key])
  return {
    keys,
    values
  }
}
