/**
 */
package org.gemoc.executionframework.xdsml_base;

import org.eclipse.emf.common.util.EList;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Editor Project</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.gemoc.executionframework.xdsml_base.EditorProject#getFileExtension <em>File Extension</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.gemoc.executionframework.xdsml_base.Xdsml_basePackage#getEditorProject()
 * @model abstract="true"
 * @generated
 */
public interface EditorProject extends ProjectResource {

	/**
	 * Returns the value of the '<em><b>File Extension</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File Extension</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File Extension</em>' attribute list.
	 * @see org.gemoc.executionframework.xdsml_base.Xdsml_basePackage#getEditorProject_FileExtension()
	 * @model
	 * @generated
	 */
	EList<String> getFileExtension();
} // EditorProject
