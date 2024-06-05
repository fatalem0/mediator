import { Avatar, Box, Typography } from "@mui/material";
import { Stack } from "@mui/material";

interface IChatRoomHeader {
	interlocutorAccountName: string
	interlocutorImageUrl: string
}

export function ChatRoomHeader({ interlocutorAccountName, interlocutorImageUrl }: IChatRoomHeader) {
	return (
		<Box sx={{ height: "8%", borderBottom: "1px solid" }}>
			<Stack direction="row" alignItems="center" spacing={1} sx={{ p: "0.5em" }}>
				<Avatar
					src={interlocutorImageUrl}
					sx={{ width: "40px", height: "40px" }}
				/>
				<Typography>{interlocutorAccountName}</Typography>
			</Stack>
		</Box>
	)
}
