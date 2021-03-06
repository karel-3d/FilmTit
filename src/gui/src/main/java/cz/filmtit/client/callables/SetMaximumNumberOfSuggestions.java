/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.client.callables;

import cz.filmtit.client.Gui;
import cz.filmtit.client.pages.Settings;

/**
 * Set maximum number of suggestions to show for each line.
 */
public class SetMaximumNumberOfSuggestions extends SetSetting<Integer> {

    /**
     * Set maximum number of suggestions to show for each line.
     * Does <b>not</b> enqueue the call immediately,
     * call enqueue() explicitly!
     */
	public SetMaximumNumberOfSuggestions(Integer setting, Settings settingsPage) {
		super(setting, settingsPage);
	}

	@Override
	protected void call() {
		filmTitService.setMaximumNumberOfSuggestions(Gui.getSessionID(), setting, this);
	}

}

