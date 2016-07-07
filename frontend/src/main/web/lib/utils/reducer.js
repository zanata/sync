import { API_DONE, API_ERROR, API_IN_PROGRESS } from '../constants/commonStateKeys'

// TODO use immutable js
export const requestHandler = (state, action) => {
  return {
    ...state,
    [API_IN_PROGRESS]: true,
    [API_DONE]: false,
    [API_ERROR]: null
  }
}

export const errorHandler = (state, action) => {
  console.error('api error:', action.payload)
  return {
    ...state,
    [API_ERROR]: action.payload,
    [API_IN_PROGRESS]: false,
    [API_DONE]: true
  }
}

export const commonSuccessState = (state) => {
  return {
    ...state,
    [API_IN_PROGRESS]: false,
    [API_DONE]: true,
    [API_ERROR]: null
  }
}

