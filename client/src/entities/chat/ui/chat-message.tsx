import { Box, Typography } from "@mui/material";
import "../model/types"
import { IChatMessage } from "../model/types";

export function ChatMessage({ id, userId, content, timeSent }: IChatMessage, flex: string) {
	return (
		<Box sx={{
			border: "1px solid",
			p: "0.2em",
			mx: "1em",
			my: "0.1em",
			maxHeight: "150%",
			maxWidth: "45%",
			alignSelf: userId == "my" ? "flex-end" : "flex-start"
		}}>
			<Typography>{content}</Typography>
			<Typography align="right">{timeSent}</Typography>
		</Box>
	)
}
