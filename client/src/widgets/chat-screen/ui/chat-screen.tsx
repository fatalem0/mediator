import { Box, Typography } from "@mui/material"
import { IChat } from "../../../entities/chat"
import { ChatRoom } from "../../chat-room"
import { UserChat } from "../../../entities/user"

interface IChatScreen {
	userId: string
	selectedChat: UserChat | undefined
	interlocutorId: string
	interlocutorAccountName: string
	interlocutorImageUrl: string
}

export function ChatScreen(
	{
		userId,
		selectedChat,
		interlocutorId,
		interlocutorAccountName,
		interlocutorImageUrl
	}: IChatScreen
) {
	return (
		<Box sx={{ display: "flex", flexDirection: "column", height: "100%", width: "100%" }}>
			{selectedChat ?
				(
					<ChatRoom
						interlocutorId={interlocutorId}
						interlocutorAccountName={interlocutorAccountName}
						interlocutorImageUrl={interlocutorImageUrl}
					/>
				) : (
					<Box sx={{ height: "100%", display: "flex", flexDirection: "column", placeContent: "center" }}>
						<Typography align="center">Выберите, кому бы вы хотели написать</Typography>
					</Box>
				)
			}
		</Box>
	)
}
