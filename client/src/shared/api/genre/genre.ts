import { apiInstance } from "../base"
import { IGenre } from "./types"

const BASE_URL = 'genres'

export const getGenres = (): Promise<IGenre[]> => {
	return apiInstance.get(BASE_URL)
}
