import { useNavigate } from "react-router-dom"
import Button from "../../components/UI/Button/Button"
import Grid from "../../components/UI/Grid/Grid"
import GridItem from "../../components/UI/Grid/GridItem/GridItem"
import Content from "../../components/views/Content/Content"
import "./WarningPage.pcss"
import { AppRoutes } from "../../types/const"

function WarningPage() {
  const navigate = useNavigate()

  function onClick() {
	return navigate(AppRoutes.main , { replace: true})
  }

  return (
	<>
	  <Content className="warning-page" classNameBody="warning-page__body">
		<Grid className="warning-page__grid" columns={2}>
		  <GridItem className="warning-page__grid__text-item">
		    <h1>
	          Данное приложение не предназначено для поиска романтических отношений.
	          Любой флирт и другие разновидности ухаживания запрещены.
            </h1>
		  </GridItem>
		  <GridItem className="warning-page__grid__button-disagree-item">
		    <Button
			  className="warning-page__button"
			  classNameBody="warning-page__button__body"
			  onClick={onClick}
			>
			  Выйти
			</Button>
		  </GridItem>
		  <GridItem className="warning-page__grid__button-agree-item">
		    <Button
			  className="warning-page__button"
			  classNameBody="warning-page__button__body"
			>
			  Согласиться
			</Button>
		  </GridItem>
		</Grid>
	  </Content>
	</>
  )
}

export default WarningPage
