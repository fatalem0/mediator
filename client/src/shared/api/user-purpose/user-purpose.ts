import { apiInstance } from "../base"
import { IUserPurpose } from "./types"

const BASE_URL = 'user-purposes'

export const getUserPurposes = (): Promise<IUserPurpose[]> => {
	return apiInstance.get(BASE_URL)
}
