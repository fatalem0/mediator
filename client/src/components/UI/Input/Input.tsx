import classNames from "classnames"
import { forwardRef, useState } from "react"
import Label from "../Label/Label"
import Button from "../Button/Button"
import "./Input.pcss"

interface IInput {
	className?: string
	id: string
	name: string
	type?: "text" | "email" | "tel" | "date" | "password" | "url"
	value?: string
	label?: string
	onChange?: React.FormEventHandler<HTMLInputElement>
}

function Input(
	{
		className,
		id,
		name,
		type = "text",
		value = "",
		label = "",
		onChange
	}: IInput
) {
	const [ isPasswordShown, setIsPasswordShown ] = useState(false)
	const hasLabel = Boolean(label)
	const isPassword = type === "password"

	const onTogglePasswordVisibilityButtonClick = () => {
		setIsPasswordShown(!isPasswordShown)
	}

	let typeFormatted = type

	if (isPassword && isPasswordShown) {
		typeFormatted = "text"
	}

	return (
		<>
			{hasLabel && <Label htmlFor={id}>{label}</Label>}
			<div className="input-wrapper">
				{isPassword && (
					<Button
						className="input-wrapper__password-button"
						classNameBody="input-wrapper__password-button__body"
						classNameIcon="input-wrapper__password-button__body__icon"
						icon={isPasswordShown ? "eye-open" : "eye-closed"}
						onClick={onTogglePasswordVisibilityButtonClick}
					/>
				)}
				<input
					className={classNames(className, "input")}
					id={name}
					name={name}
					type={typeFormatted}
					value={value}
					onChange={onChange}
				/>
			</div>
		</>
  )
}

export default forwardRef(Input)
