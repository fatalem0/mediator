import axios from "axios"

const REST_API_BASE_URL = 'http://localhost:8080/api/v1/'

export const login = (creds: UserCredentials) => axios.post(REST_API_BASE_URL + 'login', creds)
export const register = (creds: UserCredentials) => axios.post(REST_API_BASE_URL + 'register', creds)
