import { useCallback, useContext, useEffect, useState } from "react";
import { Header } from "../../../widgets/app-header";
import { ChatCard } from "../../../widgets/chat-card";
import { Box } from "@mui/material";
import { ChatsPreview } from "../../../widgets/chats-preview";
import { IChat, IChatMessage } from "../../../entities/chat/model/types";
import { ChatScreen } from "../../../widgets/chat-screen";
import { UserContext } from "../../../models/UserContext";
import { useInfiniteQuery } from "@tanstack/react-query";
import { UserChat, getUserChats } from "../../../entities/user";

export function MessengerPage() {
	const context = useContext(UserContext)
	// const [chats, setChats] = useState<UserChat[]>([])
	const [selectedChat, setSelectedChat] = useState<UserChat>()

	const escFunction = useCallback((event) => {
    if (event.key === "Escape") {
			setSelectedChat(undefined)
    }
  }, []);

	useEffect(() => {
    document.addEventListener("keydown", escFunction, false);

    return () => {
      document.removeEventListener("keydown", escFunction, false);
    };
  }, [escFunction]);

	const { data, fetchNextPage, hasNextPage } = useInfiniteQuery({
		queryKey: ['user_chats'],
		queryFn: async ({ pageParam }) => getUserChats(context.userId, pageParam),
		initialPageParam: 0,
		getNextPageParam: (lastPage) => {
			const nextOffset = lastPage.prevOffset + 8

			return nextOffset > 39 ? undefined : nextOffset
		},
	})

	// useEffect(() => {
	// 	if (data?.pages) {
	// 			const existingIds = new Set(chats.map(chat => chat.id));
	// 			const newChats = data.pages.flatMap(page =>
	// 					page.chats?.filter(chat => !existingIds.has(chat.id))
	// 						// .map(potentialFriend => ({
	// 						// 	id: potentialFriend.id,
	// 						// 	name: artist.name,
	// 						// 	genreName: artist.genreName,
	// 						// 	imageUrl: artist.imageUrl,
	// 						// 	isSelected: !!favoriteArtistButtons.find(a => a.id === artist.id)?.isSelected
	// 						// }))
	// 			);
	// 			setChats(prev => [...prev, ...newChats]);
	// 	}
	// }, [data, chats])

	// useEffect(() => {
	// 	const myChats = [
	// 		{
	// 			id: "7",
	// 			interlocutorImageUrl: "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRq-v2Gs6cC6K1P0RLMbh8R3ZmmHZlM4ZVBQgce1Fqp2w&s",
	// 			interlocutorAccountName: "Артем",
	// 			messages: [
	// 				{
	// 					id: "1",
	// 					content: "Привет!",
	// 					timeSent: "20:30"
	// 				},
	// 				{
	// 					id: "2",
	// 					content: "abu be",
	// 					timeSent: "20:40"
	// 				},
	// 			] as IChatMessage[]
	// 		},
	// 		{
	// 			id: "6",
	// 			interlocutorImageUrl: "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQkd8wWS3CaDmvzWxiQBT0prfG5Dx0GATTq0Ek9nGhWGm9D1SZKyFHvkTYJm2uTX8qGvEI&usqp=CAU",
	// 			interlocutorAccountName: "Кирилл",
	// 			messages: [
	// 				{
	// 					id: "1",
	// 					content: "Хеллоу ворлд",
	// 					timeSent: "15:30"
	// 				}
	// 			] as IChatMessage[]
	// 		},
	// 		{
	// 			id: "5",
	// 			interlocutorImageUrl: "https://static.vecteezy.com/system/resources/thumbnails/036/442/721/small_2x/ai-generated-portrait-of-a-young-man-no-facial-expression-facing-the-camera-isolated-white-background-ai-generative-photo.jpg",
	// 			interlocutorAccountName: "Артем",
	// 			messages: [
	// 				{
	// 					id: "1",
	// 					content: "Есть 5 минут?",
	// 					timeSent: "14:34"
	// 				},
	// 				{
	// 					id: "2",
	// 					content: "abu be",
	// 					timeSent: "20:40"
	// 				},
	// 			] as IChatMessage[]
	// 		},
	// 		{
	// 			id: "4",
	// 			interlocutorImageUrl: "https://cdnstorage.sendbig.com/unreal/female.webp",
	// 			interlocutorAccountName: "Лена",
	// 			messages: [
	// 				{
	// 					id: "1",
	// 					content: "Нашла интересную песню, вот",
	// 					timeSent: "13:59"
	// 				}
	// 			] as IChatMessage[]
	// 		},
	// 		{
	// 			id: "3",
	// 			interlocutorImageUrl: "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcToWG6nmXadKg1mireYKPIcen8JL8zMmJ621P8ni-29OihyRFSWnVTl0A6F-Y8fh3jz9tU&usqp=CAU",
	// 			interlocutorAccountName: "странный ник",
	// 			messages: [
	// 				{
	// 					id: "1",
	// 					content: "научишь играть на гитаре?",
	// 					timeSent: "10:51"
	// 				},
	// 				{
	// 					id: "2",
	// 					content: "abu be",
	// 					timeSent: "10:23"
	// 				},
	// 			] as IChatMessage[]
	// 		},
	// 		{
	// 			id: "2",
	// 			interlocutorImageUrl: "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQC1PxrQ847jfqtVSIsaoVD7IZV8dFTOlutqqX0gibDEljK5jJAXnHBMsL2s1bdzFtVJ9k&usqp=CAU",
	// 			interlocutorAccountName: "Андрей",
	// 			messages: [
	// 				{
	// 					id: "1",
	// 					content: "Завтра концерт",
	// 					timeSent: "0:10"
	// 				}
	// 			] as IChatMessage[]
	// 		},
	// 	] as IChat[]

	// 	setChats(myChats)
	// }, [])

	const handleSelectChat = (chat: UserChat) => {
    setSelectedChat(chat)
  }

	const chats = data?.pages.map(page => page.chats).reduce((acc, potentialFriend) => {
		return [...acc, ...potentialFriend]
	}, [])

	return (
		<Box sx={{ height: "100vh", overflowY: "hidden" }}>
			<Header />
			<Box sx={{ display: "flex", flexDirection: "row", width: "100%", height: "inherit", overflowY: "hidden" }}>
				<ChatsPreview
					chats={chats}
					selectedChat={selectedChat}
					handleSelectChat={handleSelectChat}
				/>
				<ChatScreen
					userId={context.userId}
					selectedChat={selectedChat}
					interlocutorId="1"
					interlocutorAccountName="Артем"
					interlocutorImageUrl="https://static.vecteezy.com/system/resources/thumbnails/036/442/721/small_2x/ai-generated-portrait-of-a-young-man-no-facial-expression-facing-the-camera-isolated-white-background-ai-generative-photo.jpg"
				/>
			</Box>
		</Box>
	)
}
