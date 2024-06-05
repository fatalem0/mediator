import { Box, Card, CardActionArea, CardMedia, Typography } from "@mui/material"

interface IGenreCard {
	genreName: string
	imageUrl: string
	isSelected: boolean
	onClick: () => void
}

export const GenreCard = ({ genreName, imageUrl, isSelected, onClick }: IGenreCard) => {
	return (
		<Card sx={{ maxWidth: 700, maxHeight: 500, boxShadow: isSelected ? "10px 10px 10px #0050FF" : "none" }}>
			<CardActionArea
				onClick={onClick}
			>
				<CardMedia
					component="img"
					height="250"
					image={imageUrl}
					sx={{ borderRadius: 2 }}
				>
				</CardMedia>
				<Box sx={{ position: 'absolute', top: 0, width: '100%', margin: 2 }}>
					<Typography
						gutterBottom
						variant="overline"
						sx={{
							fontSize: 15,
							backgroundColor: "white",
							p: "5px",
							borderRadius: "10px",
							color: "black"
						}}
					>
						{genreName}
					</Typography>
				</Box>
			</CardActionArea>
		</Card>
	)
}
