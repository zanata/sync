

export const requestHandler = (state, action) => {
  return {
    ...state,
    loading: true
  }
}

export const errorHandler = (state, action) => {
  console.error('api error:', action.payload)
  return {
    ...state,
    error: action.payload,
    loading: false
  }
}

