import { apiInstance } from "../../../shared/api/base"
import {
	IFavoriteArtistIds,
	IFavoriteGenreIds,
	IUserData,
	IUserPurposeIds,
	UserChatsQuery,
	UserPotentialFriendsQuery
} from "../model/types"

const BASE_URL = 'user'

export const updateUser = (userId: string, userData: IUserData): Promise<string> => {
	return apiInstance.put(`${BASE_URL}/${userId}/update`, userData)
}

export const updateUserFavoriteArtists = (userId: string, favoriteArtistIds: IFavoriteArtistIds): Promise<void> => {
	return apiInstance.put(`${BASE_URL}/${userId}/update/favorite-artists`, favoriteArtistIds)
}

export const updateUserFavoriteGenres = (userId: string, favoriteGenreIds: IFavoriteGenreIds): Promise<void> => {
	return apiInstance.put(`${BASE_URL}/${userId}/update/favorite-genres`, favoriteGenreIds)
}

export const updateUserPurposes = (userId: string, userPurposeIds: IUserPurposeIds): Promise<void> => {
	return apiInstance.put(`${BASE_URL}/${userId}/update/user-purposes`, userPurposeIds)
}

export const getUserPotentialFriends = (userId: string, offset: number = 0): Promise<UserPotentialFriendsQuery> => {
	return apiInstance.get(`${BASE_URL}/${userId}/potential-friends?limit=100&offset=${offset}`)
}

export const getUserChats = (userId: string, offset: number = 0): Promise<UserChatsQuery> => {
	return apiInstance.get(`${BASE_URL}/${userId}/chats?limit=25&offset=${offset}`)
}
