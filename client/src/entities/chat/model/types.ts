export interface IChatMessage {
	id: string
	userId: string
	content: string
	timeSent: string
}

export interface IChat {
	id: string
	interlocutorImageUrl: string
	interlocutorAccountName: string
	messages: IChatMessage[]
}

export interface CreateChatRequest {
	initiatorId: string
	friendId: string
}
