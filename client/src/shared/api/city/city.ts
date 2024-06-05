import { apiInstance } from "../base"
import { ICity } from "./types"

const BASE_URL = 'cities'

export const getCities = (): Promise<ICity[]> => {
	return apiInstance.get(BASE_URL)
}
