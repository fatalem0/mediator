import { useCallback, useEffect, useState } from "react"
import { ChatMessage, IChatMessage } from "../../../entities/chat"
import { Box, Typography } from "@mui/material"
import { ChatRoomHeader } from "../../chat-room-header"
import { ChatRoomFooter } from "../../chat-room-footer"
import { Unstable_Grid2 as Grid } from "@mui/material"
import { ChatRoomContent } from "../../chat-room-content"
import useWebSocket, { ReadyState } from 'react-use-websocket';

interface IChatRoom {
	interlocutorId: string
	interlocutorAccountName: string
	interlocutorImageUrl: string
}

export function ChatRoom(
	{
		interlocutorId,
		interlocutorAccountName,
		interlocutorImageUrl
	}: IChatRoom
) {
	const [socketUrl, setSocketUrl] = useState('ws://0.0.0.0:8080/ws');
	const [currentMessage, setCurrentMessage] = useState<string>("")
	const [messages, setMessages] = useState<any[]>([])

	const { sendMessage, lastMessage, readyState } = useWebSocket(socketUrl);

	useEffect(() => {
    if (lastMessage !== null) {
      setMessages((prev) => prev.concat(lastMessage.data));
    }
  }, [lastMessage]);

	const handleOnSubmit = useCallback(() => sendMessage('Hello'), []);

	useEffect(() => {
		const myMessages = [
			{
				id: "1",
				userId: "not_my",
				content: "Привет!",
				timeSent: "14:31"
			},
			{
				id: "2",
				userId: "my",
				content: "привет",
				timeSent: "14:32"
			},
			{
				id: "3",
				userId: "not_my",
				content: "Есть 5 минут?",
				timeSent: "14:34"
			},
		].sort((a, b) => parseInt(b.timeSent[-1]) - parseInt(a.timeSent[-1])) as IChatMessage[]

		setMessages(myMessages)
	}, [])

	// useEffect(() => {
	// 	const socket = new WebSocket('ws://0.0.0.0:8080')

	// 	socket
	// }, [messages])

	// useEffect(() => {
	// 	socket.onclose = function (event) {
	// 		console.log("CLOSED")
	// 	}
  //   // socket.on('serverMessage', (data: IChatMessage) => {
  //   //   setMessages((prev) => [...prev, data]);
  //   // });
  // }, [socket, messages]);

	// useEffect(() => {
  //   // Listen for incoming messages
	// 	socket?.onmessage = (event) => {
	// 		const message = JSON.parse(event.data)
	// 		setMessages((prevMessages) => [...prevMessages, message]);
	// 	}
  //   // socket?.onmessage('message', function message(message) {
  //   //   setMessages((prevMessages) => [...prevMessages, message]);
  //   // });
  // }, []);

	console.log(messages)

	const myUserId = "my"

	// function handleOnSubmit(e: React.FormEvent) {
	// 	e.preventDefault()

	// 	if (socket) {
	// 		console.log('Sending message...')
	// 		socket.send(currentMessage)
	// 	}

	// 	setCurrentMessage("")
	// }

	return (
		// <Box sx={{
		// 	width: "100%",
		// 	overflowY: "scroll"
		// }}>
		<>
			<ChatRoomHeader
				interlocutorAccountName={interlocutorAccountName}
				interlocutorImageUrl={interlocutorImageUrl}
			/>
			<ChatRoomContent
				userId={myUserId}
				messages={messages}
			/>
			<ChatRoomFooter
				handleOnSubmit={handleOnSubmit}
			/>
		</>
		// </Box>
	)
}
