import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import SnitchesHeader from '@site/src/components/SnitchesHeader';
import CodeSnippet from '@site/src/components/CodeSnippet';
import Heading from '@theme/Heading';

import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx(styles.heroBanner)}>
      <div className="container">
        <img 
          src={require('@site/static/img/snitchmain.png').default} 
          alt="Snitch Logo" 
          className="titleLogo" 
          style={{maxHeight: '500px', width: 'auto'}}
        />
        <div className={styles.buttons}>
        </div>
      </div>
    </header>
  );
}

export default function Home(): ReactNode {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`${siteConfig.title} - Production-grade HTTP layer for your applications`}
      description="Create a production-grade HTTP layer for your applications and (micro)services with minimal effort, and generate complete documentation with no effort.">
      <HomepageHeader />
      <SnitchesHeader />
      <CodeSnippet />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
