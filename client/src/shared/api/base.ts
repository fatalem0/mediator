import axios from "axios"
import { ApiError } from "./types"

const API_BASE_URL = 'http://localhost:8080/api/v1/'

export const apiInstance = axios.create({
	baseURL: API_BASE_URL,
	headers: {
		'Content-Type': 'application/json'
	}
})

apiInstance.interceptors.response.use(
	response => response.data,
	function (error) {
		let apiError: ApiError = {
			errorCode: error.response.data.errorCode,
			errorMsg: error.response.data.errorMsg
		}

		return Promise.reject(apiError)
	}
)
