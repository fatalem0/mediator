import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardMedia from '@mui/material/CardMedia';
import Typography from '@mui/material/Typography';
import { CardActionArea } from '@mui/material';

interface IArtistCard {
	artistName: string
	genreName: string
	imageUrl: string
	onClick: () => void
	isSelected: boolean
}

function ArtistCard({ artistName, genreName, imageUrl, onClick, isSelected }: IArtistCard) {
	return (
		<Card
			sx={{
				maxWidth: 700,
				maxHeight: 500,
				boxShadow: isSelected ? "0 0 10px #0050ff;" : "none",
				p: "10px",
				border: "0.05em solid black",
				borderRadius: "16px"
			}}
		>
			<CardActionArea
				onClick={onClick}
			>
				<CardMedia
					component="img"
					height="250"
					image={imageUrl}
					sx={{ borderRadius: "16px" }}
				>
				</CardMedia>
				<CardContent>
					<Typography gutterBottom variant="h5" component="div" fontSize={25} fontWeight={1000}>
						{artistName}
					</Typography>
				</CardContent>
			</CardActionArea>
		</Card>
	)
}

export default ArtistCard
