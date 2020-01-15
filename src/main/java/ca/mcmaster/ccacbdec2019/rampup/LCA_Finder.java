/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.rampup;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author tamvadss
 * given a root node, finds all the LCA nodes in the tree
 * 
 * in the writeup, show pseudo code blocks for methods in this file
 * 
 */
public class LCA_Finder {
    
    private NodeAttachment rootNodeAttachment; 
    private List<NodeAttachment> perfectLCANodes = new ArrayList<NodeAttachment> ();
    
    public LCA_Finder (NodeAttachment rootNodeData ){
        rootNodeAttachment =rootNodeData;
         
        //mark nodes as perfect or not
        checkIfPerfect   (rootNodeAttachment) ;
    }
    
    public  List<NodeAttachment> getPerfectLCANodes () {
        return Collections.unmodifiableList(perfectLCANodes)  ;
    }
    
    /*public  List<NodeAttachment> getPerfectLCANodes () {
        return getPerfectLCANodes (rootNodeAttachment) ;
    }*/
    
    public List<NodeAttachment> getLeafNodesIncludedInLCA (NodeAttachment perfectLcaNode){
        List<NodeAttachment> leafs = new ArrayList<NodeAttachment> ();
        
        if ( perfectLcaNode.leftChildReference==null && perfectLcaNode.rightChildReference==null){
            //this is a leaf , collect it 
            leafs.add (perfectLcaNode);
        } else {
            leafs.addAll(getLeafNodesIncludedInLCA(perfectLcaNode.leftChildReference));
            leafs.addAll(getLeafNodesIncludedInLCA(perfectLcaNode.rightChildReference));
        }
        
        return leafs;
    }
    
    
    
    //a non-leaf node is a perfect LCA node if it has 2 kids both of which are perfect
    private boolean checkIfPerfect ( NodeAttachment attachment) {
        
        boolean isPerfect = false;
        
        if ((attachment.leftChildReference==null) &&( attachment.rightChildReference==null)) {
            //mark every leaf as perfectly packed
            isPerfect=true;    
            perfectLCANodes.add (attachment) ;
        }else    if ((attachment.leftChildReference!=null) && (attachment.rightChildReference!=null )) {
            //non leaf node having 2 kids
            boolean leftChildSIPerfect = checkIfPerfect ( attachment.leftChildReference);
            boolean rightChildIsPerfect =  checkIfPerfect (attachment.rightChildReference) ;
            isPerfect= leftChildSIPerfect && rightChildIsPerfect;    
            
            if ( isPerfect) {
                //add attachment to list of perfect LCA nodes, and remove both kids of attachment from the list
                perfectLCANodes.add (attachment) ;
                perfectLCANodes.remove (attachment.leftChildReference) ;
                perfectLCANodes.remove (attachment.rightChildReference) ;
                attachment.lpRelaxedValue = Math.min (attachment.leftChildReference.lpRelaxedValue, attachment.rightChildReference.lpRelaxedValue) ;
            }
            
        } else if (attachment.leftChildReference!=null){
            checkIfPerfect ( attachment.leftChildReference);
        }else {
            checkIfPerfect (attachment.rightChildReference) ;
        }
                
        return isPerfect;
    }
    
}
