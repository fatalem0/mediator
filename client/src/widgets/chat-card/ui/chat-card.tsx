import { Avatar, Box, Card, CardActionArea, CardContent, Typography } from "@mui/material";

interface IChatCard {
	interlocutorImageUrl?: string
	interlocutorAccountName?: string
	lastSentMessage?: string
	lastTimeMessageSent?: string
	isSelected: boolean
	handleOnClick: () => void
}

export function ChatCard(
	{
		interlocutorImageUrl,
		interlocutorAccountName,
		lastSentMessage,
		lastTimeMessageSent,
		isSelected,
		handleOnClick
	}: IChatCard
) {
	return (
		<Card sx={{ width: "100%", height: "13%", backgroundColor: isSelected ? "#97C6F7" : "transparent" }}>
			<CardActionArea
				onClick={handleOnClick}
				sx={{ display: 'flex', flexDirection: 'row', height: "100%", justifyContent: "flex-start" }}
			>
				<Avatar
					src={interlocutorImageUrl}
					sx={{ width: "50px", height: "50px", m: "0.5em" }}
				/>
				<Box sx={{ display: 'flex', flexDirection: 'column', height: "100%", width: "inherit", py: "0.5em", pr: "0.1em" }}>
					<Box sx={{ display: 'flex', flexDirection: 'row', width: "inherit", px: "0.5em" }}>
						<Typography align="left" sx={{ width: "inherit" }}>{interlocutorAccountName}</Typography>
						<Typography align="right" sx={{ width: "inherit" }}>{lastTimeMessageSent}</Typography>
					</Box>
					<Typography sx={{ px: "0.5em" }}>{lastSentMessage}</Typography>
				</Box>
			</CardActionArea>
		</Card>
	)
}
