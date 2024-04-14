import classNames from "classnames"
import "./SvgIcon.pcss"

interface ISvgIcon {
	className?: string,
	prefix?: string,
	name: string,
	color?: string
}

function SvgIcon({ className, prefix = 'icon', name, color = '#333' }: ISvgIcon) {
	const symbolId = `#${prefix}-${name}`

	return (
		<svg className={classNames(className, "svg-icon")} aria-hidden="true">
			<use href={symbolId} fill={color} />
		</svg>
	)
}

export default SvgIcon
