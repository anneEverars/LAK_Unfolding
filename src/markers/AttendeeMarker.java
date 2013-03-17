package markers;

import java.util.List;

import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractMarker;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

public class AttendeeMarker extends AbstractMarker {

	PImage img;
	String organisation;
	List<String> members;
	protected float size = 15;
	protected int space = 25;
	private PFont font;

	public AttendeeMarker(Location location, PImage img, String organisation, List<String> members,PFont font) {
		super(location);
		this.img = img;
		this.organisation = organisation;
		this.members = members;
		this.font = font;
	}

	@Override
	public void draw(PGraphics pg, float x, float y) {
		pg.pushStyle();
		pg.pushMatrix();
		pg.image(img, x - 11, y - 37);
		if (selected) {
			pg.translate(0, 0, 1);
		}
		pg.strokeWeight(strokeWeight);
		if (selected) {
			pg.fill(highlightColor);
			pg.stroke(highlightStrokeColor);
		} else {
			pg.fill(color);
			pg.stroke(strokeColor);
		}
		//pg.ellipse(x, y, size, size);// TODO use radius in km and convert to px

		// label
		if (selected && organisation != null) {
			pg.textFont(font);
			pg.fill(highlightColor);
			pg.stroke(highlightStrokeColor);
			int test = 25+members.size()*25;
			pg.rect(x + strokeWeight / 2, y - font.getSize() + strokeWeight / 2 - space,
					pg.textWidth(organisation) + space * 1.5f, font.getSize() + test);
			pg.fill(255, 255, 255);
			String name = organisation;
			for(int i = 0; i<members.size();i++)
			{
				name = name.concat("\n"+members.get(i));
			}
			pg.text(name, Math.round(x + space * 0.75f + strokeWeight / 2),
					Math.round(y + strokeWeight / 2 - space * 0.75f));
		}
		pg.popMatrix();
		pg.popStyle();
	}

	@Override
	protected boolean isInside(float checkX, float checkY, float x, float y) {
		return checkX > x && checkX < x + img.width && checkY > y && checkY < y + img.height;
	}
	


}