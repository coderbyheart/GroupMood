package de.hsrm.mi.mobcomp.y2k11grp04.extra;

import java.security.InvalidParameterException;

public class TextColoring {
	public int getColorForPercent(double p) {
		if (p < 0 || p > 1)
			throw new InvalidParameterException(
					"Percent must be between 0 and 1.");
		int alpha = 255;
		int r = (int) (255 - 255 * p);
		int g = (int) (255 * p);
		int b = 0;
		return alpha << 24 | r << 16 | g << 8 | b;
	}

	public int getColorForPercent(int p) {
		if (p < 0 || p > 100)
			throw new InvalidParameterException(
					"Percent must be between 0 and 100.");
		return getColorForPercent(p / 100.0);
	}
}
