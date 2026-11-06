package br.edu.ddm.achadoseperdidos.delivery;
import java.util.Date;
public final class Delivery {
    public final String objectId, pointId, responsibleUid, description, operationId;
    public final Date createdAt;
    public Delivery(String objectId, String pointId, String responsibleUid, String description, String operationId, Date createdAt) {
        this.objectId=objectId; this.pointId=pointId; this.responsibleUid=responsibleUid; this.description=description; this.operationId=operationId; this.createdAt=new Date(createdAt.getTime());
    }
}
