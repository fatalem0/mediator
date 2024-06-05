export interface IUserData {
	email?: string
	password?: string
	accountName?: string
	about?: string
	cityId?: string
}

export interface IUserPurposeIds {
	userPurposeIds: string[]
}

export interface IFavoriteArtistIds {
	favoriteArtistIds: string[]
}

export interface IFavoriteGenreIds {
	favoriteGenreIds: string[]
}

export interface IUserPotentialFriend {
	id: string
	accountName: string
	imageURL?: string
	about: string
	city: string
	userPurposes: string[]
	favoriteGenres: string[]
	favoriteArtists: string[]
	matchingPercent: number
}

export interface UserPotentialFriendsQuery {
	prevOffset: number
	potentialFriends: IUserPotentialFriend[]
}

export interface UserChat {
	id: string
	initiatorId: string
	friendId: string
	friendAccountName?: string
	friendImageUrl?: string
	lastSentMessage?: string
	lastTimeMessageSent?: string
}

export interface UserChatsQuery {
	prevOffset: number
	chats: UserChat[]
}
