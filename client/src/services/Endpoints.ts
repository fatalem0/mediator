import axios from "axios"

const REST_API_BASE_URL = 'http://localhost:8080/api/v1/'

export const login = (loginData: LoginData) => axios.post(REST_API_BASE_URL + 'login', loginData)
