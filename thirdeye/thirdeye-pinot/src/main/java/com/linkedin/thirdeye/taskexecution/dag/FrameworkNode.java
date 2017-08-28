package com.linkedin.thirdeye.taskexecution.dag;

import com.linkedin.thirdeye.taskexecution.dataflow.ExecutionResultsReader;
import com.linkedin.thirdeye.taskexecution.impl.dag.ExecutionStatus;
import com.linkedin.thirdeye.taskexecution.impl.dag.NodeConfig;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Execution Framework Related Nodes. The difference between {@link Node} and FrameworkNode is that Node defines the
 * horizontal topology of workflow, which is given by users (e.g., data scientists). A FrameworkNode defines the
 * vertical topology of execution flow that is defined by framework developers.
 *
 * For example, Nodes form a workflow:
 *     DataPreparationNode --> AnomalyDetectionNode --> MergeAndUpdateAnomalyNode
 *
 * During the execution of the framework, FrameworkNodes define how a Node is executed across machines and threads.
 * Suppose that users use a simple FrameworkNodes which runs using one thread, then the execution flow becomes:
 *    (DataPreparationNode) --> AnomalyDetectionNode --> (MergeAndUpdateAnomaliesNode)
 *                                       |
 *                                       | provides operator
 *                                       v
 *                      AnomalyDetectionSingleThreadFrameworkNode
 *                                       |
 *                                       | initializes operator executor on local machine with one thread
 *                                       v
 *                               OperatorExecutor (runs operator)
 *
 * {@link com.linkedin.thirdeye.taskexecution.impl.dag.DAGExecutor} should remain agnostic to the vertical topology,
 * which is taken care of by FrameworkNode. On the other hand, FrameworkNode does not have the whole picture of
 * the workflow (DAG), it only knows the incoming node for preparing the input of its Processor.
 */
public abstract class FrameworkNode<K, V> implements Callable<NodeIdentifier> {
  protected NodeIdentifier nodeIdentifier = new NodeIdentifier();
  protected Class operatorClass;
  protected NodeConfig nodeConfig = new NodeConfig();

  protected FrameworkNode() {
  }

  protected FrameworkNode(NodeIdentifier nodeIdentifier, Class operatorClass) {
    this.nodeIdentifier = nodeIdentifier;
    this.operatorClass = operatorClass;
  }

  protected FrameworkNode(NodeIdentifier nodeIdentifier, NodeConfig nodeConfig, Class operatorClass) {
    this.nodeIdentifier = nodeIdentifier;
    this.nodeConfig = nodeConfig;
    this.operatorClass = operatorClass;
  }

  public NodeIdentifier getIdentifier() {
    return nodeIdentifier;
  }

  public void setNodeIdentifier(NodeIdentifier nodeIdentifier) {
    this.nodeIdentifier = nodeIdentifier;
  }

  public Class getOperatorClass() {
    return operatorClass;
  }

  public void setOperatorClass(Class operatorClass) {
    this.operatorClass = operatorClass;
  }

  public void setNodeConfig(NodeConfig nodeConfig) {
    this.nodeConfig = nodeConfig;
  }

  public NodeConfig getNodeConfig() {
    return nodeConfig;
  }

  public abstract FrameworkNode<K, V> getLogicalNode();

  public abstract Collection<FrameworkNode<K, V>> getPhysicalNode();

  public abstract ExecutionStatus getExecutionStatus();

  public abstract ExecutionResultsReader<K, V> getExecutionResultsReader();

  /**
   * {@link NodeIdentifier} is always the unique identifier to a node and hence all the other fields of this class
   * are left out on purpose.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FrameworkNode that = (FrameworkNode) o;
    return Objects.equals(nodeIdentifier, that.nodeIdentifier);
  }

  /**
   * {@link NodeIdentifier} is always the unique identifier to a node and hence all the other fields of this class
   * are left out on purpose.
   */
  @Override
  public int hashCode() {
    return Objects.hash(nodeIdentifier);
  }
}
