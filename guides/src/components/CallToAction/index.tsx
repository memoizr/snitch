import React from 'react';
import Link from '@docusaurus/Link';
import styles from './styles.module.css';

const CallToAction: React.FC = () => {
  return (
    <div className={styles.container}>
      <h2 className={styles.heading}>GET SNITCHED</h2>
      <Link
        to="/docs/Docs"
        className={styles.button}
      >
        DISCOVER HOW
      </Link>
    </div>
  );
};

export default CallToAction;