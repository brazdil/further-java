package uk.ac.cam.cl.fjava.messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class DynamicObjectInputStream extends ObjectInputStream {

	private ClassLoader current = ClassLoader.getSystemClassLoader();

	public DynamicObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException {
		try {
			return current.loadClass(desc.getName());
		}
		catch (ClassNotFoundException e) {
			return super.resolveClass(desc);
		}
	}

	public void addClass(final String name, final byte[] defn) {
		current = new ClassLoader(current) {
			@Override
			protected Class<?> findClass(String className)
					throws ClassNotFoundException {
				if (className.equals(name)) {
					Class<?> result = defineClass(name, defn, 0, defn.length);
					return result;
				} else {
					throw new ClassNotFoundException();
				}
			}
		};
	}

}
