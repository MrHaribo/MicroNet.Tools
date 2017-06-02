package micronet.tools.ui.modelview;

public class Entity {
	private String id;
	private String Type;
	private Entity parent;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public Entity getParent() {
		return parent;
	}
	public void setParent(Entity parent) {
		this.parent = parent;
	}
}
