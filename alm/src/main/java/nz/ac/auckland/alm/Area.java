package nz.ac.auckland.alm;

import nz.ac.auckland.linsolve.Constraint;
import nz.ac.auckland.linsolve.OperatorType;

/**
 * Rectangular area in the GUI, defined by a tab on each side.
 */
public class Area extends AbstractLayoutSpecArea {
	public static class Size {
		double width;
		double height;

		final public static int UNDEFINED = -2;

		public Size() {
		}

		public Size(Size size) {
			this.width = size.width;
			this.height = size.height;
		}

		public Size(double width, double height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass())
				return false;
			final Size other = (Size) obj;
			if (this.width != other.width || this.height != other.height)
				return false;
			return true;
		}

		public double getWidth() {
			return width;
		}

		public double getHeight() {
			return height;
		}

		public void setWidth(double width) {
			this.width = width;
		}

		public void setHeight(double height) {
			this.height = height;
		}
	}

	public static class Rect {
		public float left;
		public float top;
		public float right;
		public float bottom;

		public Rect(float left, float top, float right, float bottom) {
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
		}

		public float getWidth() {
			return right - left;
		}

		public float getHeight() {
			return bottom - top;
		}

		public boolean contains(float x, float y) {
			if (left > x || right < x)
				return false;
			if (top > y || bottom < y)
				return false;
			return true;
		}

		public boolean intersects(Rect area) {
			return intersects(area.left, area.top, area.right, area.bottom);
		}

		public boolean intersects(float left, float top, float right, float bottom) {
			boolean hIntersection = (left >= this.left && left <= this.right)
					|| (right >= this.left && right <= this.right) || (left <= this.left && right >= this.right);
			boolean vIntersection = (top >= this.top && top <= this.bottom)
					|| (bottom >= this.top && bottom <= this.bottom) || (top <= this.top && bottom >= this.bottom);
			return hIntersection && vIntersection;
		}
	}

	/**
	 * Undefined size. Used if a certain size constraint is not set.
	 */
	static final Size UNDEFINED_SIZE = new Size(Size.UNDEFINED, Size.UNDEFINED);
	static final public double PREFERRED_SIZE_PENALTY = 0.5;

	// size constraint for the content
	Constraint minWidthConstraint;
	Constraint maxWidthConstraint;
	Constraint minHeightConstraint;
	Constraint maxHeightConstraint;
	Constraint preferredWidthConstraint;
	Constraint preferredHeightConstraint;

	final Size shrinkPenalty = new Size();
	final Size growPenalties = new Size(1, 1);

	Size minSize = new Size(0, 0);
	Size preferredSize = UNDEFINED_SIZE;
	Size maxSize = new Size(Integer.MAX_VALUE, Integer.MAX_VALUE);

	int leftInset = 0;
	int topInset = 0;
	int rightInset = 0;
	int bottomInset = 0;

	HorizontalAlignment hAlignment = HorizontalAlignment.CENTER;
	VerticalAlignment vAlignment = VerticalAlignment.CENTER;

	double aspectRatio = -1;
	Constraint aspectRatioConstraint;

	@Override
	public ILayoutSpecArea clone(XTab clonedLeft, YTab clonedTop, XTab clonedRight, YTab clonedBottom) {
		Area clone = new Area(clonedLeft, clonedTop, clonedRight, clonedBottom);
		clone.setAlignment(getHorizontalAlignment(), getVerticalAlignment());
		clone.setMinSize(getMinSize());
		clone.setPreferredSize(getPreferredSize());
		clone.setMaxSize(getMaxSize());
		return clone;
	}

	private void updateHorizontalConstraintVars() {
		minWidthConstraint.setLeftSide(-1, left, 1, right);

		// if a maximum constraint was set, then it needs to use the new tab
		if (maxWidthConstraint != null)
			maxWidthConstraint.setLeftSide(-1, left, 1, right);

		if (preferredWidthConstraint != null)
			preferredWidthConstraint.setLeftSide(-1, left, 1, right);

		invalidateLayoutSpec();
	}

	private void updateVerticalConstraintVars() {
		// the respective minimum constraint needs to use the new tab
		minHeightConstraint.setLeftSide(-1, top, 1, bottom);

		// if a maximum constraint was set, then it needs to use the new tab
		if (maxHeightConstraint != null)
			maxHeightConstraint.setLeftSide(-1, top, 1, bottom);

		if (preferredHeightConstraint != null)
			preferredHeightConstraint.setLeftSide(-1, top, 1, bottom);

		invalidateLayoutSpec();
	}

	/**
	* Set the left tab of the area
	* @param value The tab specification to be used.
	*/
	public void setLeft(XTab value) {
		super.setLeft(value);
		updateHorizontalConstraintVars();
	}

	/**
	* Set the rigth tab of the area
	* @param value The tab specification to be used.
	*/
	public void setRight(XTab value) {
		super.setRight(value);
		updateHorizontalConstraintVars();
	}

	/**
	* Set the top tab of the area
	* @param value the tab specification to be used.
	*/
	public void setTop(YTab value) {
		super.setTop(value);
		updateVerticalConstraintVars();
	}

	/**
	* Set the bottom tab of the area
	* @param value the tab specification to be used.
	*/
	public void setBottom(YTab value) {
		super.setBottom(value);
		updateVerticalConstraintVars();
	}

	public void setTo(XTab left, YTab top, XTab right, YTab bottom) {
		setLeftRight(left, right);
		setTopBottom(top, bottom);
	}

	private void setLeftRight(XTab left, XTab right) {
		super.setLeft(left);
		super.setRight(right);
		updateHorizontalConstraintVars();
	}

	private void setTopBottom(YTab top, YTab bottom) {
		super.setTop(top);
		super.setBottom(bottom);
		updateVerticalConstraintVars();
	}

	/**
	 * Minimum size of the area's content. May be different from the minimum
	 * size of the area.
	 */
	public Size getMinSize() {
		return minSize;
	}

	private Size addSpacingAndInset(Size value) {
		// Add insets and 2 times the half of the horizontal and vertical spacing.
		return new Size(value.getWidth() + getLayoutHSpacing() + getLeftInset() + getRightInset(),
				value.getHeight() + getLayoutHSpacing() + getTopInset() + getBottomInset());
	}

	/**
	* Set the minimum size of the area's content.
	* @param value Size that defines the desired minimum size.
	*/
	public void setMinSize(Size value) {
        minSize = value;
		Size effectiveSize = addSpacingAndInset(value);
		minWidthConstraint.setRightSide(effectiveSize.getWidth());
		minHeightConstraint.setRightSide(effectiveSize.getHeight());

		invalidateLayoutSpec();
	}

    public void setMinSize(double width, double height) {
        setMinSize(new Size(width, height));
    }

	/**
	 * Maximum size of the area's content. May be different from the maximum
	 * size of the area.
	 */
	public Size getMaxSize() {
		return maxSize;
	}
	/**
	* Set the maximal size of the area's content.
	* @param value Size that defines the desired maximal size.
	*/
	public void setMaxSize(Size value) {
        maxSize = value;
		Size effectiveSize = null;
		if (maxSize.getWidth() > 0 || maxSize.getHeight() > 0)
			effectiveSize = addSpacingAndInset(value);

		if (maxSize.getWidth() > 0) {
			if (maxWidthConstraint == null) {
				maxWidthConstraint = new Constraint(-1, left, 1, right, OperatorType.LE, 0,	growPenalties.getWidth());
				addConstraint(maxWidthConstraint);
			}
			updateRightSideHorizontal(maxWidthConstraint, effectiveSize.getWidth());
		} else if (maxWidthConstraint != null) {
			removeConstraint(maxWidthConstraint);
			maxWidthConstraint = null;
		}

		if (maxSize.getHeight() > 0) {
			if (maxHeightConstraint == null) {
				maxHeightConstraint = new Constraint(-1, top, 1, bottom, OperatorType.LE, 0, growPenalties.getHeight());
				addConstraint(maxHeightConstraint);
			}
			updateRightSideVertical(maxHeightConstraint, effectiveSize.getHeight());
		} else if (maxHeightConstraint != null) {
			removeConstraint(maxHeightConstraint);
			maxHeightConstraint = null;
		}

		invalidateLayoutSpec();
	}

    public void setMaxSize(double width, double height) {
        setMaxSize(new Size(width, height));
    }

	/**
	 * Preferred size of the area's content. May be different from the preferred
	 * size of the area. Manual changes of PreferredContentSize are ignored
	 * unless autoPreferredContentSize is set to false.
	 */
	public Size getPreferredSize() {
		return preferredSize;
	}
	/**
	* Set the preferred size of the area's content. Manual changes
	* of PreferredContentSize are ignored unless
	* autoPreferredContentSize is set to false.
	* @param value Size that defines the preferred size.
	*/
	public void setPreferredSize(Size value) {
        preferredSize = value;
		Size effectiveSize = null;
		if (preferredSize.getWidth() > 0 || preferredSize.getHeight() > 0)
			effectiveSize = addSpacingAndInset(value);

		if (preferredSize.getWidth() > 0) {
			if (preferredWidthConstraint == null) {
				preferredWidthConstraint = new Constraint(-1, left, 1, right, OperatorType.EQ, 0, shrinkPenalty.width);
				addConstraint(preferredWidthConstraint);
			}
			updateRightSideHorizontal(preferredWidthConstraint, effectiveSize.getWidth());
		} else if (preferredWidthConstraint != null) {
			removeConstraint(preferredWidthConstraint);
			preferredWidthConstraint = null;
		}

		if (preferredSize.getHeight() > 0) {
			if (preferredHeightConstraint == null) {
				preferredHeightConstraint = new Constraint(-1, top, 1, bottom, OperatorType.EQ, 0,
						shrinkPenalty.height);
				addConstraint(preferredHeightConstraint);
			}
			updateRightSideVertical(preferredHeightConstraint, effectiveSize.getHeight());
		} else if (preferredHeightConstraint != null){
			removeConstraint(preferredHeightConstraint);
			preferredHeightConstraint = null;
		}

		invalidateLayoutSpec();
	}

    public void setPreferredSize(double width, double height) {
        setPreferredSize(new Size(width, height));
    }

	/**
	 * The reluctance with which the area's content shrinks below its preferred
	 * size. The bigger the less likely is such shrinking.
	 */
	public double getShrinkPenaltyWidth() {
		return shrinkPenalty.width;
	}

    public double getShrinkPenaltyHeight() {
        return shrinkPenalty.height;
    }

	/**
	* Set the reluctance with which the area's content shrinks below its preferred
	* size. The bigger the less likely is such shrinking.
	*/
	public void setShrinkPenalties(double width, double height) {
		shrinkPenalty.setWidth(width);
		shrinkPenalty.setHeight(height);

		// penalties are only relevant if a preferred size is set
		if (preferredWidthConstraint != null) {
			// TODO: was previously setPenaltyNeg; maybe use two
			// inequalities for preferredWidthConstraint
			preferredWidthConstraint.setPenalty(width);
			preferredHeightConstraint.setPenalty(height);
		}

		invalidateLayoutSpec();
	}

	/**
	 * The reluctance with which the area's content grows over its preferred
	 * size. The bigger the less likely is such expansion.
	 */
	public Size getGrowPenalties() {
		return growPenalties;
	}
	/**
	* Set the reluctance with which the area's content grows over its preferred
	* size. The bigger the less likely is such expansion.
	* @param value Size that defines the grow penalties.
	*/
	public void setGrowPenalties(Size value) {
		growPenalties.setWidth(value.width);
		growPenalties.setHeight(value.height);
		if (preferredWidthConstraint != null) { // penalties are only relevant
			// if a
			// preferred size is set
			preferredWidthConstraint.setPenalty(value.getWidth());
			preferredHeightConstraint.setPenalty(value.getHeight());
		}

		invalidateLayoutSpec();
	}

	/**
	 * Aspect ratio of the area's content. May be different from the aspect
	 * ratio of the area.
	 */
	public double getAspectRatio() {
		return aspectRatio;
	}
	/**
	* Set the aspect ratio of the area's content. May be different from the aspect
	* ratio of the area.
	* @param value double that defines the desired aspect ratio.
	*/
	public void setAspectRatio(double value) {
		aspectRatio = value;
		if (aspectRatio > 0) {
			if (aspectRatioConstraint == null) {
				aspectRatioConstraint = new Constraint(-1, left, 1, right, aspectRatio, top, -aspectRatio, bottom,
						OperatorType.EQ, 0);
				addConstraint(aspectRatioConstraint);
			} else
				aspectRatioConstraint.setLeftSide(-1, left, 1, right, aspectRatio, top, -aspectRatio, bottom);
		} else if (aspectRatioConstraint != null) {
			removeConstraint(aspectRatioConstraint);
			aspectRatioConstraint = null;
		}

		invalidateLayoutSpec();
	}

	/**
	 * Horizontal alignment of the content in its area.
	 */
	public HorizontalAlignment getHorizontalAlignment() {
		return hAlignment;
	}

	/**
	 * Set the horizontal alignment of the content in its area.
	 * @param value HorizontalAlignment to set
	 */
	public void setHorizontalAlignment(HorizontalAlignment value) {
		hAlignment = value;
	}

	/**
	 * Vertical alignment of the content in its area.
	 */
	public VerticalAlignment getVerticalAlignment() {
		return vAlignment;
	}
	/**
	 * Set the vertical alignment of the content in its area.
	 * @param value VerticalAlignment to set
	 */
	public void setVerticalAlignment(VerticalAlignment value) {
		vAlignment = value;
	}

	public void setAlignment(HorizontalAlignment hAlignment, VerticalAlignment vAlignment) {
		setHorizontalAlignment(hAlignment);
		setVerticalAlignment(vAlignment);
	}

	private float relativeHorizontal(HorizontalAlignment alignment) {
		switch (alignment) {
			case LEFT:
				return 0;
			case RIGHT:
				return 1;
			case CENTER:
				return 0.5f;
		}
		return 0;
	}

	private float relativeVertical(VerticalAlignment alignment) {
		switch (alignment) {
			case TOP:
				return 0;
			case BOTTOM:
				return 1;
			case CENTER:
				return 0.5f;
		}
		return 0;
	}

	private float getLayoutHSpacing() {
		return layoutSpec == null ? 0 : layoutSpec.getHorizontalSpacing();
	}

	private float getLayoutVSpacing() {
		return layoutSpec == null ? 0 : layoutSpec.getVerticalSpacing();
	}

	/**
	 * Get the area's content rect.
	 *
	 * This takes the inset, alignment and layout spacing into account. This rect can, for example, be used to position
	 * a widget on the screen.
	 *
	 * @return The area's content rect.
	 */
	public Rect getContentRect() {
		float hSpacing2 = getLayoutHSpacing() / 2;
		float vSpacing2 = getLayoutVSpacing() / 2;
		Rect frame = new Rect((float)(getLeft().getValue() + getLeftInset() + hSpacing2),
				(float)(getTop().getValue() + getTopInset() + vSpacing2),
				(float)(getRight().getValue() - getRightInset() - hSpacing2),
				(float)(getBottom().getValue() - getBottomInset() - vSpacing2));

		// Taken from the Haiku source code:
		// align according to the given alignment
		if (maxSize.width < frame.getWidth() && hAlignment != HorizontalAlignment.FILL) {
			frame.left += (frame.getWidth() - maxSize.width) * relativeHorizontal(hAlignment);
			frame.right = (float)(frame.left + maxSize.width);
		}
		if (maxSize.height < frame.getHeight() && vAlignment != VerticalAlignment.FILL) {
			frame.top += (frame.getHeight() - maxSize.height) * relativeVertical(vAlignment);
			frame.bottom = (float)(frame.top + maxSize.height);
		}

		return frame;
	}

	/**
	 * Left inset between area and its content.
	 */
	public int getLeftInset() {
		return leftInset;
	}
	/**
	 * Set the left inset between area and its content.
	 * @param value int that defines the left inset
	 */
	public void setLeftInset(int value) {
		leftInset = value;
		updateHorizontal();
		invalidateLayoutSpec();
	}

	/**
	 * Top inset between area and its content.
	 */
	public int getTopInset() {
		return topInset;
	}
	/**
	 * Set the top inset between area and its content.
	 * @param value int that defines the top inset
	 */
	public void setTopInset(int value) {
		topInset = value;
		updateVertical();
		invalidateLayoutSpec();
	}

	/**
	 * Right inset between area and its content.
	 */
	public int getRightInset() {
		return rightInset;
	}
	/**
	 * Set the right inset between area and its content.
	 * @param value int that defines the right inset
	 */
	public void setRightInset(int value) {
		rightInset = value;
		updateHorizontal();
		invalidateLayoutSpec();
	}

	/**
	 * Bottom inset between area and its content.
	 */
	public int getBottomInset() {
		return bottomInset;
	}
	/**
	 * Set the bottom inset between area and its content.
	 * @param value int that defines the bottom inset
	 */
	public void setBottomInset(int value) {
		bottomInset = value;
		updateVertical();
		invalidateLayoutSpec();
	}

	void updateRightSideHorizontal(Constraint constraint, double rightSide) {
		constraint.setRightSide(rightSide + leftInset + rightInset);
	}

	/**
	 * Update the constraints for horizontal insets and alignment.
	 */
	void updateHorizontal() {
		updateRightSideHorizontal(minWidthConstraint, minSize.getWidth());
		if (preferredWidthConstraint != null)
			updateRightSideHorizontal(preferredWidthConstraint, preferredSize.getWidth());
		if (maxWidthConstraint != null)
			updateRightSideHorizontal(maxWidthConstraint, maxSize.getWidth());
	}

	void updateRightSideVertical(Constraint constraint, double rightSide) {
		constraint.setRightSide(rightSide + topInset + bottomInset);
	}

	/**
	 * Update the constraints for vertical insets and alignment.
	 */
	void updateVertical() {
		updateRightSideVertical(minHeightConstraint, minSize.getHeight());
		if (preferredWidthConstraint != null)
			updateRightSideVertical(preferredWidthConstraint, preferredSize.getHeight());
		if (maxWidthConstraint != null)
			updateRightSideVertical(maxWidthConstraint, maxSize.getHeight());
	}

	/**
	* Returns a string that displays the area's layout specification
	* @return string identifies the area's layout specification
	*/
	public String toString() {
		String string = "";
		if (getId() != null)
			string += getId();
		else
			string += "Area";
		string += "(";
		if (left != null)
			string += left.toString();
		else
			string += "NULL";
		string += ",";
		if (top != null)
			string += top.toString();
		else
			string += "NULL";
		string += ",";
		if (right != null)
			string += right.toString();
		else
			string += "NULL";
		string += ",";
		if (bottom != null)
			string += bottom.toString();
		else
			string += "NULL";
		string += ")";
		return string;
	}

	public Area() {
		this(null, null, null, null);
	}

	/**
	* Construct the area based on the given tabs
	* @param left the left vertical grid line
	* @param top the top horizontal grid line
	* @param right the right vertical grid line
	* @param bottom the bottom horizontal grid line
	*/
	public Area(XTab left, YTab top, XTab right, YTab bottom) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;

		// adds the two essential constraints of the area that make sure that
		// the left x-tab is really
		// to the left of the right x-tab, and the top y-tab really above the
		// bottom y-tab
		// TODO: the preferred size constraints need to have a smaller penalty
		// (not INFINITY as per default)
		minWidthConstraint = new Constraint(-1, left, 1, right, OperatorType.GE,
				minSize.getWidth());
		minWidthConstraint.setName("minWidthConstraint");
		addConstraint(minWidthConstraint);
		minHeightConstraint = new Constraint(-1, top, 1, bottom, OperatorType.GE,
				minSize.getHeight());
		minHeightConstraint.setName("minHeightConstraint");
		addConstraint(minHeightConstraint);

		setGrowPenalties(new Size(0, 0));
 		setShrinkPenalties(PREFERRED_SIZE_PENALTY, PREFERRED_SIZE_PENALTY);
	}

	@Override
	public void attachedToLayoutSpec(LayoutSpec layoutSpec) {
		super.attachedToLayoutSpec(layoutSpec);

		// take the layout spec spacing into account
		setMinSize(minSize);
		setPreferredSize(preferredSize);
		setMaxSize(maxSize);
	}

}
