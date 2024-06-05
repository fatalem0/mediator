import { AppBar, Avatar, Box, Button, Container, IconButton, SvgIcon, Toolbar, Tooltip, Typography } from "@mui/material"
import './header.pcss'
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../app/routers/endpoints"

export const Header = () => {
	const navigate = useNavigate()

	function handleOnMessengerButtonClick() {
		return navigate(AppRoutes.messenger, { replace: true})
	}

	function handleOnFeedButtonClick() {
		return navigate(AppRoutes.feed, { replace: true})
	}

	const pages = [
		'Лента',
		'Мессенджер'
	]

	return (
		<AppBar position="static" sx={{ backgroundColor: 'white', borderBottom: "1px solid black", boxShadow: 0 }}>
			<Container maxWidth="xl">
				<Toolbar disableGutters className='header'>
					<SvgIcon className='header__logo' name="mediator-logo"/>
					<Typography
						variant="h6"
						color='black'
					>
						Mediator
					</Typography>
					<Box className='header__button-container' sx={{ mx: 2 }}>
						<Button
							key="Лента"
							onClick={handleOnFeedButtonClick}
							sx={{ my: 2, color: 'black', display: 'block' }}
						>
							Лента
						</Button>
						<Button
							key="Сообщения"
							onClick={handleOnMessengerButtonClick}
							sx={{ my: 2, color: 'black', display: 'block' }}
						>
							Сообщения
						</Button>
          </Box>
					<Tooltip title="Перейти в профиль">
						<IconButton>
							<Avatar>R</Avatar>
						</IconButton>
					</Tooltip>
				</Toolbar>
			</Container>
		</AppBar>
	)
}
