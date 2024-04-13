import { ReactNode } from "react"
import classNames from "classnames"
import "./PageTitle.pcss"

interface IPageTitle {
  className?: string
  children: ReactNode
}

function PageTitle({ className, children }: IPageTitle) {
  return <h1 className={classNames(className, "page-title")}>{children}</h1>
}

export default PageTitle
