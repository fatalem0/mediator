import { useEffect, useState } from "react";
import { ChatMessage, IChatMessage } from "../../../entities/chat";
import { Box, Unstable_Grid2 as Grid, Stack } from "@mui/material"
import { io, Socket } from 'socket.io-client'

interface IChatRoomContent {
	userId: string
	messages: IChatMessage[]
}

export function ChatRoomContent({ userId, messages }: IChatRoomContent) {

	const myMessages = messages.filter((message: IChatMessage) => message.userId == userId)
	const interlocutorMessages = messages.filter((message: IChatMessage) => message.userId != userId)

	return (
		<Box sx={{ display: "flex", flexDirection: "column", overflowY: "auto", height: "80%" }}>
			{/* <Stack spacing={1}> */}
				{messages.map((interlocutorMessage: IChatMessage) => (
					<ChatMessage
						id={interlocutorMessage.id}
						userId={interlocutorMessage.userId}
						content={interlocutorMessage.content}
						timeSent={interlocutorMessage.timeSent}
					/>
				))}
			{/* </Stack> */}
			{/* <Stack spacing={1} direction="column"> */}
				{/* {myMessages.map((myMessage: IChatMessage) => (
						<ChatMessage
							id={myMessage.id}
							userId={myMessage.userId}
							content={myMessage.content}
							timeSent={myMessage.timeSent}
						/>
				))} */}
			{/* </Stack> */}
		</Box>
		// <Grid container direction="row" alignContent="space-between" sx={{ width: "100%", height: "100%" }}>
		// 	<Grid container direction="column" columns={1} alignContent="start" sx={{ border: "1px solid", height: "40%", width: "50%", flexWrap: "none", WebkitFlexWrap: "none", overflowY: "scroll" }}>
		// 		{interlocutorMessages.map((interlocutorMessage: IChatMessage) => (
		// 			<Grid sx={{ mx: "1.7em", my: "0.3em" }}>
		// 				<ChatMessage
		// 					id={interlocutorMessage.id}
		// 					userId={interlocutorMessage.userId}
		// 					content={interlocutorMessage.content}
		// 					timeSent={interlocutorMessage.timeSent}
		// 				/>
		// 			</Grid>
		// 		))}
		// 	</Grid>
		// 	<Grid container direction="column" alignContent="end" sx={{ border: "1px solid", height: "40%", width: "50%" }}>
		// 		{myMessages.map((myMessage: IChatMessage) => (
		// 			<Grid sx={{ mx: "1.7em", my: "0.3em" }}>
		// 				<ChatMessage
		// 					id={myMessage.id}
		// 					userId={myMessage.userId}
		// 					content={myMessage.content}
		// 					timeSent={myMessage.timeSent}
		// 				/>
		// 			</Grid>
		// 		))}
		// 	</Grid>
		// </Grid>
	)
}
