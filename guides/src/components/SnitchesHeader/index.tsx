import React, { useEffect, useRef } from 'react';
import styles from './styles.module.css';

interface SnitchesHeaderProps {
  className?: string;
}

const SnitchesHeader: React.FC<SnitchesHeaderProps> = ({ className }) => {
  const topTextRef = useRef<HTMLHeadingElement>(null);
  const bottomTextRef = useRef<HTMLHeadingElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const calculateProportionalFontSize = () => {
      if (topTextRef.current && bottomTextRef.current) {
        // Get the exact width of the top text after font is loaded
        const topTextWidth = topTextRef.current.getBoundingClientRect().width;
        const bottomTextNaturalWidth = bottomTextRef.current.getBoundingClientRect().width;
        
        // Get the base font size of both texts
        const topFontSize = parseFloat(window.getComputedStyle(topTextRef.current).fontSize);
        const bottomFontSize = parseFloat(window.getComputedStyle(bottomTextRef.current).fontSize);
        
        // Calculate scale factor to make bottom text the same width as top text
        const scaleFactor = topTextWidth / bottomTextNaturalWidth;
        
        // Apply the calculated font size
        const newBottomFontSize = bottomFontSize * scaleFactor;
        bottomTextRef.current.style.fontSize = `${newBottomFontSize}px`;
      }
    };

    // Font loading can take time, so we need to wait for it
    const fontLoader = () => {
      if (document.fonts && document.fonts.ready) {
        document.fonts.ready.then(() => {
          // Once fonts are loaded, calculate sizes
          calculateProportionalFontSize();
          
          // Also add a slight delay to ensure rendering is complete
          setTimeout(calculateProportionalFontSize, 100);
        });
      } else {
        // Fallback if document.fonts is not available
        setTimeout(calculateProportionalFontSize, 500);
      }
    };

    fontLoader();
    window.addEventListener('resize', calculateProportionalFontSize);
    
    return () => {
      window.removeEventListener('resize', calculateProportionalFontSize);
    };
  }, []);

  return (
    <div className={`${styles.container} ${className || ''}`}>
      <div className={styles.bar}></div>
      <div className={styles.textContainer} ref={containerRef}>
        <h1 className={styles.titleTop} ref={topTextRef}>SNITCHES</h1>
        <h2 className={styles.titleBottom} ref={bottomTextRef}>ON YOUR API</h2>
      </div>
      <div className={styles.bar}></div>
    </div>
  );
};

export default SnitchesHeader;