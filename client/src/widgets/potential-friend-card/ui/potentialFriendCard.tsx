import Card from '@mui/material/Card'
import './potentialFriendCard.pcss'
import CardHeader from '@mui/material/CardHeader'
import { Avatar, Button, CardContent, Snackbar, Typography } from '@mui/material'
import { red } from '@mui/material/colors'
import { Unstable_Grid2 as Grid } from "@mui/material"
import { useContext, useState } from 'react'
import { UserContext } from '../../../models/UserContext'
import { CreateChatRequest, createChat } from '../../../entities/chat'
import { ApiError } from '../../../shared/api/types'

interface PotentialFriendCard {
	id: string
	imageURL?: string
	accountName: string
	city: string
	about: string
	userPurposes: string[]
	favoriteGenres: string[]
	favoriteArtists: string[]
	matchingPercent: number
}

export const PotentialFriendCard = (
	{
		id,
		imageURL,
		accountName,
		city,
		about,
		userPurposes,
		favoriteGenres,
		favoriteArtists,
		matchingPercent
	}: PotentialFriendCard
) => {
	const context = useContext(UserContext)
	const [isMessageSent, setIsMessageSent] = useState(false);

  const handleClose = (event: React.SyntheticEvent | Event, reason?: string) => {
    if (reason === 'clickaway') {
      return;
    }

    setIsMessageSent(false);
  };

	const genres = [
		'Поп',
    'Рок',
    'Инди',
    'Метал',
    'Электроника',
    'Рэп и хип-хоп',
		'Эстрада',
    'Классическая музыка'
	]

	const artists = [
		'The Beatles',
		'Nirvana',
		'Led Zeppelin',
		'Дайте Танк (!)',
		'Кино',
		'Браво',
		'Секрет',
		'Король и Шут',
		'a-ha',
		'The Police',
		// 'Taylor Swift',
		// 'Ранетки',
		// 'Макс Корж',
		// 'Антонио Вивальди',
		// 'Эрик Сати',
		// 'WILLOW',
		// 'Daft Punk',
		// 'Crystal Castles',
		// 'Metallica',
		// 'Mastodon'
	]

	// const userPurposes = [
	// 	"Обмен музыкой",
	// 	"Собрать группу",
	// 	"Пойти на концерт",
	// 	"Создавать музыку",
	// 	"Обучение музыке"
	// ]

	const handleOnClick = async () => {
		const createChatRequest: CreateChatRequest = {
			initiatorId: context.userId,
			friendId: id
		}

		await createChat(createChatRequest)
			.then(function () {
				console.log(`Chat for initiator = ${context.userId} and friend = ${id} has been successfully created`)
				setIsMessageSent(true)
			})
			.catch(function (error: ApiError) {
				console.log(error)
			})
	}

	return (
		<Card variant="outlined" className='potential-friend-card' sx={{ borderRadius: '16px' }}>
			<CardHeader
				avatar={
					<Avatar src={imageURL} sx={{ bgcolor: red[500], height: 100, width: 100 }} aria-label="recipe">
            R
          </Avatar>
				}
				title={accountName}
				subheader={city}
				action={
					<Typography
						variant="body2"
						sx={{ position: 'relative', top: 16, right: 16, fontWeight: 'bold' }}
					>
            Cовпадение: {matchingPercent}%
          </Typography>
				}
			>
			</CardHeader>
			<CardContent>
				<Typography variant="body2" color="text.secondary">
					{about}
				</Typography>
				<Typography
					variant="body2"
					color="text.disabled"
					gutterBottom
					sx={{ mt: 1, mb: 1 }}
				>
					Интересы
				</Typography>
				<Grid container direction="row" spacing={2}>
					{userPurposes.map((genre) => (
						<Grid className="potential-friend-card__favorite">
							<Typography variant="body2" color="text.primary" sx={{ fontWeight: 'bold' }}>
								{genre}
							</Typography>
						</Grid>
					))}
				</Grid>
				<Typography
					variant="body2"
					color="text.disabled"
					gutterBottom
					sx={{ mt: 1, mb: 1 }}
				>
					Любимые жанры
				</Typography>
				<Grid container spacing={2}>
					{favoriteGenres.map((genre) => (
						<Grid className="potential-friend-card__favorite">
							<Typography variant="body2" color="text.primary" sx={{ fontWeight: 'bold' }}>
								{genre}
							</Typography>
						</Grid>
					))}
				</Grid>
				<Typography
					variant="body2"
					color="text.disabled"
					gutterBottom
					sx={{ mt: 1, mb: 1 }}
				>
					Любимые группы
				</Typography>
				<Grid container rowSpacing={2} columnSpacing={2}>
					{favoriteArtists.map((artist) => (
						<Grid className="potential-friend-card__favorite">
							<Typography variant="body2" color="text.primary" sx={{ fontWeight: 'bold' }}>
								{artist}
							</Typography>
						</Grid>
					))}
				</Grid>
				<Button
					variant="contained"
					color="primary"
					onClick={handleOnClick}
					sx={{ position: 'relative', right: 0, bottom: 0, float: 'right' }}
				>
					Написать
				</Button>
				<Snackbar
					open={isMessageSent}
					autoHideDuration={4000}
					onClose={handleClose}
					message='Создан чат с пользователем'
				/>
			</CardContent>
		</Card>
	)
}
