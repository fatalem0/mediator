import classNames from "classnames"
import Container from "../../UI/Container/Container"
import "./Content.pcss"

interface IContent {
  className?: string
  classNameBody?: string
  children: React.ReactNode
}

function Content({ className, classNameBody, children }: IContent) {
  return (
	<div className={classNames(className, "content")}>
	  <Container className={classNames(classNameBody, "content__body")}>
		{children}
	  </Container>
	</div>
  )
}

export default Content
