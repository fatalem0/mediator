import { apiInstance } from "../../../shared/api/base"
import {
	CreateChatRequest
} from "../model/types"

const BASE_URL = 'chat'

export const createChat = (req: CreateChatRequest): Promise<void> => {
	return apiInstance.post(`${BASE_URL}/create`, req)
}
