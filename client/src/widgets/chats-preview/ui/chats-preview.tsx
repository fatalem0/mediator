import { Box } from "@mui/system"
import { ChatCard } from "../../chat-card"
import { IChat } from "../../../entities/chat"
import { useInfiniteQuery } from "@tanstack/react-query"
import { Stack } from "@mui/material"
import { UserChat } from "../../../entities/user"

interface IChatsPreview {
	chats?: UserChat[]
	selectedChat?: UserChat
	handleSelectChat: (chat: UserChat) => void
}

export function ChatsPreview({ chats, selectedChat, handleSelectChat }: IChatsPreview) {
	// const { data, fetchNextPage, hasNextPage } = useInfiniteQuery({
	// 	queryKey: ['chats-preview'],
	// 	queryFn: async ({ pageParam }) => getArtists(pageParam),
	// 	initialPageParam: 0,
	// 	getNextPageParam: (lastPage) => {
	// 		const nextOffset = lastPage.prevOffset + 6

	// 		return nextOffset > 19 ? undefined : nextOffset
	// 	},
	// })

	return (
		<Box sx={{
			height: "90%",
			width: "35%",
			// display: "flex",
			// flexFlow: "column nowrap",
			// flexBasis: "auto",
			// width: "500px",
			// // height: "100%",
			borderRight: "1px solid",
			overflowY: "scroll"
		}}>
			{chats?.map((chat: UserChat) => (
				<ChatCard
					interlocutorImageUrl={chat.friendImageUrl}
					interlocutorAccountName={chat.friendAccountName}
					lastSentMessage={chat.lastSentMessage}
					lastTimeMessageSent={chat.lastTimeMessageSent}
					isSelected={selectedChat?.id === chat.id}
					handleOnClick={() => handleSelectChat(chat)}
				/>
			))}
		</Box>
	)
}
