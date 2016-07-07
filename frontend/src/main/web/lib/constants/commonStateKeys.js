/**
 * list of common keys in reducer state
 */

/**
 * Indicates whether some API call is in progress. A sign to show some
 * progress/loading bar. This key will store a boolean to be true or false.
 * @type {string}
 */
export const API_IN_PROGRESS = 'API_IN_PROGRESS'

/**
 * Indicates whether some API call is done and this key will store a boolean to
 * be true or false.
 * @type {string}
 */
export const API_DONE = 'API_DONE'

/**
 * Indicates some API call has resulted in error and this key will store the
 * error action payload.
 * @type {string}
 */
export const API_ERROR = 'API_ERROR'
