package org.gemoc.executionengine.java.sequential_modeling_workbench.ui.debug

import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashSet
import java.util.List
import java.util.Map
import java.util.Properties
import org.eclipse.core.runtime.Platform
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.impl.EObjectImpl
import org.osgi.framework.Bundle

class IntrospectiveMutableFieldExtractor implements MutableFieldExtractor {

	private String bundleSymbolicName
	private Bundle bundle
	private Map<EObject, List<MutableField>> eObjects = new HashMap
	private Map<EClass, List<Pair<Class<?>, Class<?>>>> aspectClasses = new HashMap
	private Properties properties

	new(String bundleSymbolicName) {
		this.bundleSymbolicName = bundleSymbolicName
		this.bundle = Platform.getBundle(bundleSymbolicName)
	}

	private def String capitalize(String string) {
		val c = string.toCharArray()
		c.set(0, Character.toUpperCase(c.get(0)))
		return new String(c)
	}

	private def String decapitalize(String string) {
		val c = string.toCharArray()
		c.set(0, Character.toLowerCase(c.get(0)))
		return new String(c)
	}

	private def String findName(Class<?> cls, EObject eObject) {
		// Find all fields of the given class that are called "name".
		val name = cls.declaredFields.filter[f|f.name.equals("name")]
		if (name.empty) {
			// Try again on superclass if it's not EObjectImpl.
			if (cls.superclass != EObjectImpl) {
				return findName(cls.superclass, eObject)
			}
			// No field called "name" accessible on the given EObject, return null.
			return null
		} else {
			// A field called "name" has been found, return its value.
			val f = name.get(0)
			f.accessible = true
			return f.get(eObject).toString
		}
	}

	private def String findId(Class<?> cls, EObject eObject) {
		// Find all fields of the given class that are called "id".
		val id = cls.declaredFields.filter[f|f.name.equals("id")]
		if (id.empty) {
			// Try again on superclass if it's not EObjectImpl.
			if (cls.superclass != EObjectImpl) {
				return findId(cls.superclass, eObject)
			}
			// No field called "id" accessible on the given EObject, return null.
			return null
		} else {
			// A field called "id" has been found, return its value.
			val f = id.get(0)
			f.accessible = true
			return f.get(eObject).toString
		}
	}

	private def String findDataName(EObject eObject) {
		val name = findName(eObject.class, eObject)
		if (name == null) {
			val id = findId(eObject.class, eObject)
			if (id == null) {
				return eObject.toString
			} else {
				return decapitalize(eObject.eClass.name) + " " + id
			}
		} else {
			return name
		}
	}

	private def List<MutableField> getMutableFieldsFromAspect(EObject eObject, Class<?> properties, Class<?> aspect) {

		val result = new ArrayList

		val fields = properties.fields

		if (!fields.empty) {
			fields.forEach [ f |
				val methods = aspect.methods.filter[m|m.name.equals(f.name)]
				val getter = methods.findFirst[m|m.parameterCount == 1]
				val setter = methods.findFirst[m|m.parameterCount == 2]
				if (getter != null && setter != null) {
					val data = new MutableField(
						findDataName(eObject),
						eObject,
						[getter.invoke(null, eObject)],
						[t|setter.invoke(null, eObject, t)]
					)
					result.add(data)
				}
			]
		}

		return result
	}

	override extractMutableField(EObject eObject) {
		if (!eObjects.containsKey(eObject)) {
			val datas = new ArrayList
			if (!aspectClasses.containsKey(eObject.eClass)) {
				val classes = getStaticHelperClasses(eObject)
				if (classes != null) {
					val list = new ArrayList
					classes.forEach [ i, l |
						l.forEach [ c |
							try {
								val properties = bundle.loadClass(c.name + i.simpleName + "AspectProperties")
								val pair = new Pair(c, properties)
								list.add(pair)
								datas.addAll(getMutableFieldsFromAspect(eObject, properties, c))
							} catch (ClassNotFoundException e) {
							}
						]
					]
					aspectClasses.put(eObject.eClass, list)
				} else {
					aspectClasses.put(eObject.eClass, Collections.EMPTY_LIST)
				}
			} else {
				val list = aspectClasses.get(eObject.eClass)
				list.forEach [ p |
					datas.addAll(getMutableFieldsFromAspect(eObject, p.value, p.key))
				]
			}
			eObjects.put(eObject, datas)
			return datas
		} else {
			return eObjects.get(eObject)
		}
	}

	private def getSuperInterfacesOfInterface(Class<?> c, HashSet<Class<?>> set) {
		val possibleSuperInterfaces = Arrays.asList(c.interfaces).filter[i|!i.equals(EObject)]
		possibleSuperInterfaces.forEach [ i |
			if (set.add(i)) {
				getSuperInterfacesOfInterface(i, set)
			}
		]
	}

	private def List<Class<?>> getSuperInterfacesOfInterface(Class<?> c) {
		if (c == null) {
			return Collections.EMPTY_LIST
		}
		val interfacesFound = new LinkedHashSet<Class<?>>()
		getSuperInterfacesOfInterface(c, interfacesFound)
		return new ArrayList<Class<?>>(interfacesFound)
	}

	private def List<Class<?>> getInterfacesOfEObject(EObject o) {
		val List<Class<?>> possibleInterfaces = new ArrayList
		val List<Class<?>> interfaces = getAllInterfaces(o.class);
		val baseInterface = interfaces.findFirst[i|i.simpleName.equals(o.eClass.name)]
		if (baseInterface != null) {
			possibleInterfaces.add(baseInterface)
			possibleInterfaces.addAll(getSuperInterfacesOfInterface(baseInterface))
		}
		println(possibleInterfaces)
		return possibleInterfaces
	}

	private def List<Class<?>> getAllInterfaces(Class<? extends EObject> cls) {
		if (cls == null) {
			return Collections.EMPTY_LIST
		}
		val interfacesFound = new LinkedHashSet<Class<?>>()
		getAllInterfaces(cls, interfacesFound)
		val res = new ArrayList<Class<?>>(interfacesFound)
		return res
	}

	private def getAllInterfaces(Class<?> cls, HashSet<Class<?>> interfacesFound) {
		var currCls = cls;
		while (currCls != null) {
			currCls.getInterfaces().forEach [ i |
				if (interfacesFound.add(i)) {
					getAllInterfaces(i, interfacesFound)
				}
			]
			currCls = currCls.getSuperclass()
		}
	}

	private def loadProperties() {
		properties = new Properties()
		val searchedPropertyFileName = "/META-INF/xtend-gen/" + bundleSymbolicName + ".k3_aspect_mapping.properties"
		var inputStream = Class.getResourceAsStream(searchedPropertyFileName)
		if (inputStream == null) {
			try {
				inputStream = bundle.getEntry(searchedPropertyFileName).openStream()
			} catch (Exception e) {
				e.printStackTrace()
				return
			}
		}
		if (inputStream != null) {
			try {
				properties.load(inputStream)
			} catch (IOException e) {
				return
			}
		}
	}

	private def Map<Class<?>, List<Class<?>>> getStaticHelperClasses(EObject target) {
		val List<Class<?>> allPossibleInterfaces = getInterfacesOfEObject(target)

		val searchedPropertyFileName = "/META-INF/xtend-gen/" + bundleSymbolicName + ".k3_aspect_mapping.properties"
		properties = new Properties()
		var inputStream = Class.getResourceAsStream(searchedPropertyFileName)
		if (inputStream == null) {
			try {
				inputStream = bundle.getEntry(searchedPropertyFileName).openStream()
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace()
				return null
			}
		}
		val Map<Class<?>, String> possibleStaticClassesNames = new HashMap
		try {
			if (inputStream != null) {
				properties.load(inputStream)
				allPossibleInterfaces.forEach [ i |
					possibleStaticClassesNames.put(i, properties.getProperty(i.getCanonicalName()))
				]
			}
		} catch (IOException e) {
			// TODO report for debug that no mapping was found
			return null
		}
		if (possibleStaticClassesNames.empty) {
			return null
		}

		val Map<Class<?>, List<Class<?>>> classes = new HashMap
		possibleStaticClassesNames.forEach [ i, s |
			if (s != null) {
				val l = new HashSet
				s.replaceAll(" ", "").split(",").forEach [ n |
					try {
						l.add(bundle.loadClass(n))
					} catch (ClassNotFoundException e) {
						// possibleException = e;
					}
				]
				classes.put(i, new ArrayList(l))
			} else {
				classes.put(i, Collections.EMPTY_LIST)
			}
		]
		if (classes.isEmpty()) {
//			Activator.getMessagingSystem().error("ClassNotFoundException, see Error Log View", Activator.PLUGIN_ID,
//					possibleException);
		}

		return classes
	}

}