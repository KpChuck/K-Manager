package jadx.core.xmlgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

import jadx.core.xmlgen.entry.ResourceEntry;

public class ResourceStorage {

	private final List<ResourceEntry> list = new ArrayList<>();
	private String appPackage;

	private Comparator<ResourceEntry> comparator = new Comparator<ResourceEntry>() {
        @Override
        public int compare(ResourceEntry o1, ResourceEntry o2) {
            return Integer.compare(o1.getId(), o2.getId());
        }
    };

	public Collection<ResourceEntry> getResources() {
		return list;
	}

	public void add(ResourceEntry ri) {
		list.add(ri);
	}

	public void finish() {

		list.sort(comparator);
	}

	public ResourceEntry getByRef(int refId) {
		ResourceEntry key = new ResourceEntry(refId);
		int index = Collections.binarySearch(list, key, comparator);
		if (index < 0) {
			return null;
		}
		return list.get(index);
	}

	public String getAppPackage() {
		return appPackage;
	}

	public void setAppPackage(String appPackage) {
		this.appPackage = appPackage;
	}

	public Map<Integer, String> getResourcesNames() {
		Map<Integer, String> map = new HashMap<>();
		for (ResourceEntry entry : list) {
			map.put(entry.getId(), entry.getTypeName() + "/" + entry.getKeyName());
		}
		return map;
	}
}
