import { apiInstance } from "../base"
import { ArtistsQuery } from "./types"

const BASE_URL = 'artists?limit=6'

export const getArtists = (offset: number = 0): Promise<ArtistsQuery> => {
	return apiInstance.get(`${BASE_URL}&offset=${offset}`)
}
