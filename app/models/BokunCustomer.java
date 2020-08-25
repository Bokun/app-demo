package models;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Set;

@Entity
public class BokunCustomer extends Model {

    @Id
    private Long id;
    private String vendorId;
    private String domain;
    private String accessToken;
    private String permissions;

    public boolean hasRequiredPermissions(String requiredPermissions) {
        Set<String> granted = Sets.newHashSet(Splitter.on(',').split(permissions));
        Set<String> needed = Sets.newHashSet(Splitter.on(',').split(requiredPermissions));
        return granted.containsAll(needed);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public static io.ebean.Finder<Long, BokunCustomer> find = new io.ebean.Finder<>(BokunCustomer.class);

    public static BokunCustomer findByVendorId(String vendorId) {
        return find.query().where().eq("vendorId", vendorId).findOneOrEmpty().orElse(null);
    }
}
