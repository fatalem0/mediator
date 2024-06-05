import axios from "axios"

const REST_API_BASE_URL = 'http://localhost:8080/api/v1/'

axios.interceptors.response.use(
	response => response,
	function (error) {
		let apiError: ApiError = {
			errorCode: error.response.data.errorCode,
			errorMsg: error.response.data.errorMsg
		}

		return Promise.reject(apiError)
	}
)

export const login = (creds: UserCredentials) => axios.post(REST_API_BASE_URL + 'login', creds)

export const register = (creds: UserCredentials) => axios.post(REST_API_BASE_URL + 'register', creds)

export const update = (userId: string, forUpdateUserData: ForUpdateUserData) =>
  axios.put(REST_API_BASE_URL + "user/" + userId + "/update", forUpdateUserData)
